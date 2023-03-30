package com.octopus.core.processor;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.processor.extractor.Collector;
import com.octopus.core.processor.extractor.ExtractorHelper;
import com.octopus.core.processor.extractor.FieldType;
import com.octopus.core.processor.extractor.InvalidExtractorException;
import com.octopus.core.processor.extractor.Result;
import com.octopus.core.processor.extractor.ValidateException;
import com.octopus.core.processor.extractor.Validator;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.LinkMethod;
import com.octopus.core.processor.extractor.annotation.Prop;
import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.extractor.selector.SelectorHandler;
import com.octopus.core.processor.extractor.selector.SelectorHandlerRegistry;
import com.octopus.core.processor.extractor.type.TypeHandler;
import com.octopus.core.processor.extractor.type.TypeHandlerRegistry;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/4/19
 */
public class ExtractorProcessor<T> implements Processor {

  private final Class<T> extractorClass;

  private final Collector<T> collector;

  public ExtractorProcessor(@NonNull Class<T> extractorClass) {
    this(extractorClass, null);
  }

  public ExtractorProcessor(@NonNull Class<T> extractorClass, Collector<T> collector) {
    this.extractorClass = extractorClass;
    try {
      Validator.getInstance().validate(extractorClass);
    } catch (ValidateException e) {
      throw new InvalidExtractorException(e);
    }
    this.collector = collector;
  }

  @Override
  public void process(Response response, Octopus octopus) {
    try {
      Result<T> result = this.extract(response.asText(), response, this.extractorClass);
      if (collector != null) {
        collector.collect(result.getObj());
      }
      result.getRequests().forEach(octopus::addRequest);
    } catch (Exception e) {
      throw new ProcessException(
          "Error process response from request ["
              + response.getRequest()
              + "] with extractor "
              + extractorClass.getName(),
          e);
    }
  }

  private <R> Result<R> extract(String source, Response response, Class<R> extractorClass) {
    R instance = ReflectUtil.newInstance(extractorClass);
    List<Request> requests = new ArrayList<>();
    Result<R> result = new Result<>(instance, requests);
    Field[] fields =
        ReflectUtil.getFields(extractorClass, f -> f.isAnnotationPresent(Selector.class));
    for (Field field : fields) {
      this.extractField(result, source, field, response);
    }
    Link[] links = extractorClass.getAnnotation(Extractor.class).links();
    for (Link link : links) {
      this.extractLink(result, link, source, response);
    }

    Method[] methods =
        ReflectUtil.getMethods(
            extractorClass, method -> method.isAnnotationPresent(LinkMethod.class));
    for (Method linksMethod : methods) {
      this.extractLinkMethod(result, linksMethod, response);
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  private void extractLinkMethod(Result<?> result, Method method, Response response) {
    Class<?>[] paramTypes = method.getParameterTypes();
    Object returnObj = null;
    if (paramTypes.length == 0) {
      returnObj = ReflectUtil.invoke(result.getObj(), method);
    } else if (paramTypes.length == 1) {
      returnObj = ReflectUtil.invoke(result.getObj(), method, response);
    }
    Collection<Object> objects = new ArrayList<>();
    if (returnObj != null) {
      if (returnObj instanceof String || returnObj instanceof Request) {
        objects.add(returnObj);
      } else if (ArrayUtil.isArray(returnObj)) {
        int length = Array.getLength(returnObj);
        for (int i = 0; i < length; i++) {
          objects.add(Array.get(returnObj, i));
        }
      } else if (Collection.class.isAssignableFrom(returnObj.getClass())) {
        objects = (Collection<Object>) returnObj;
      }

      objects.forEach(
          item -> {
            if (item instanceof String) {
              String u = item.toString();
              result.getRequests().add(Request.get(completeUrl(response.getRequest().getUrl(), u)));
            } else if (item instanceof Request) {
              result.getRequests().add((Request) item);
            }
          });
    }
  }

  private void extractLink(Result<?> result, Link link, String source, Response response) {
    Set<String> urls = new HashSet<>(ListUtil.toList(link.url()));
    if (link.selectors().length > 0) {
      for (Selector selector : link.selectors()) {
        SelectorHandler selectorHandler =
            SelectorHandlerRegistry.getInstance().getSelectorHandler(selector.type());
        List<String> selected = selectorHandler.select(source, selector, true, response);
        if (selected != null) {
          urls.addAll(selected);
        }
      }
    }
    for (String url : urls) {
      if (StrUtil.isNotBlank(url)) {
        Request request =
            new Request(completeUrl(response.getRequest().getUrl(), url), link.method())
                .setPriority(link.priority())
                .setRepeatable(link.repeatable());
        Arrays.stream(link.headers())
            .forEach(p -> request.addHeader(p.name(), resolveValueFromProp(result.getObj(), p)));
        Arrays.stream(link.params())
            .forEach(p -> request.addParam(p.name(), resolveValueFromProp(result.getObj(), p)));
        Arrays.stream(link.attrs())
            .forEach(p -> request.putAttribute(p.name(), resolveValueFromProp(result.getObj(), p)));
        request.setInherit(link.inherit());
        result.getRequests().add(request);
      }
    }
  }

  private String resolveValueFromProp(Object target, Prop prop) {
    if (StrUtil.isNotBlank(prop.field())) {
      Field field = ReflectUtil.getField(target.getClass(), prop.field());
      Object val = ReflectUtil.getFieldValue(target, field);
      return val == null ? null : val.toString();
    }
    return prop.value();
  }

  private String completeUrl(String currentUrl, String url) {
    if (!HttpUtil.isHttp(url) && !HttpUtil.isHttps(url)) {
      if (url.startsWith("/")) {
        return URLUtil.completeUrl(currentUrl, url);
      } else {
        url =
            UrlBuilder.of(currentUrl).setQuery(UrlQuery.of(url, CharsetUtil.CHARSET_UTF_8)).build();
      }
    }
    return url;
  }

  @SuppressWarnings("unchecked")
  private void extractField(Result<?> result, String source, Field field, Response response) {
    Selector selector = field.getAnnotation(Selector.class);
    SelectorHandler selectorHandler =
        SelectorHandlerRegistry.getInstance().getSelectorHandler(selector.type());
    FieldType fieldType = ExtractorHelper.getFieldType(field);
    boolean multi = fieldType.isArray() || fieldType.isCollection();
    List<String> selected = selectorHandler.select(source, selector, multi, response);
    if (selected != null && !selected.isEmpty()) {
      if (fieldType.isArray() || fieldType.isCollection()) {
        List<Object> list = new ArrayList<>();
        Class<?> componentClass = fieldType.getComponentClass();
        for (String item : selected) {
          if (!(componentClass.isAnnotationPresent(Extractor.class))) {
            TypeHandler<?> typeHandler =
                TypeHandlerRegistry.getInstance().getTypeHandler(componentClass);
            Annotation annotation = null;
            if (typeHandler.getSupportAnnotationType() != null) {
              annotation = field.getAnnotation(typeHandler.getSupportAnnotationType());
            }
            Object obj = typeHandler.handle(item, annotation);
            list.add(obj);
          } else {
            Result<?> r = extract(item, response, componentClass);
            if (r.getRequests() != null) {
              result.getRequests().addAll(r.getRequests());
            }
            list.add(r.getObj());
          }
        }

        if (fieldType.isArray()) {
          ReflectUtil.setFieldValue(result.getObj(), field, list.toArray());
        } else {
          Class<?> collectionClass =
              TypeHandlerRegistry.getInstance()
                  .getCollectionImplClass(fieldType.getCollectionClass());
          Collection<Object> collection =
              (Collection<Object>) ReflectUtil.newInstance(collectionClass);
          collection.addAll(list);
          ReflectUtil.setFieldValue(result.getObj(), field, collection);
        }

      } else if (fieldType.getComponentClass().isAnnotationPresent(Extractor.class)) {
        Result<?> r = extract(selected.get(0), response, fieldType.getComponentClass());
        if (r.getRequests() != null) {
          result.getRequests().addAll(r.getRequests());
        }
        ReflectUtil.setFieldValue(result.getObj(), field, r.getObj());
      } else {
        TypeHandler<?> typeHandler =
            TypeHandlerRegistry.getInstance().getTypeHandler(fieldType.getComponentClass());
        Annotation annotation = null;
        if (typeHandler.getSupportAnnotationType() != null) {
          annotation = field.getAnnotation(typeHandler.getSupportAnnotationType());
        }
        Object obj = typeHandler.handle(selected.get(0), annotation);
        ReflectUtil.setFieldValue(result.getObj(), field, obj);
      }
    }
  }
}
