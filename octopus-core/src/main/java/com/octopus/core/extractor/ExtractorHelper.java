package com.octopus.core.extractor;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.extractor.annotation.Attr;
import com.octopus.core.extractor.annotation.Body;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.LinkMethod;
import com.octopus.core.extractor.annotation.Param;
import com.octopus.core.extractor.annotation.Prop;
import com.octopus.core.extractor.annotation.Url;
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.processor.matcher.Matcher;
import com.octopus.core.processor.matcher.Matchers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class ExtractorHelper {

  private static final Set<Class<?>> VALID_EXTRACTOR_CLS = new HashSet<>();

  private static boolean checkIsValidClass(@NonNull Class<?> type) {
    return Convertors.isConvertibleType(type) || checkIsValidExtractorClass(type);
  }

  private static Class<?> getActualClass(Field field) {
    Type actualType = TypeUtil.getActualType(TypeUtil.getClass(field), field);
    if (actualType != null) {
      Type[] types = TypeUtil.toParameterizedType(actualType).getActualTypeArguments();
      if (types != null && types.length > 0) {
        return TypeUtil.getClass(types[0]);
      }
    }
    return null;
  }

  public static boolean checkIsValidExtractorClass(@NonNull Class<?> type) {
    checkIsValidExtractorClass(type, new HashSet<>());
    return true;
  }

  private static void checkIsValidExtractorClass(
      @NonNull Class<?> type, Collection<Class<?>> checkedClasses)
      throws InvalidExtractorException {
    if (VALID_EXTRACTOR_CLS.contains(type)) {
      return;
    }
    if (type.isAnnotationPresent(Extractor.class)) {
      checkedClasses.add(type);
      Field[] fields = type.getDeclaredFields();
      for (Field field : fields) {
        Class<?> fieldType = TypeUtil.getClass(field);

        // check @Body
        checkIsValidBodyField(field, fieldType);

        // check @Url
        checkIsValidUrlField(field, fieldType);

        // check @Param
        checkIsValidParamField(field, fieldType);

        // check @Attr
        checkIsValidAttrField(field, fieldType);

        // check @CssSelector @JsonSelector @XpathSelector @RegexSelector
        checkIsValidSelectorFiled(field, fieldType, type, checkedClasses);

        // check format annotations
        checkIsValidFormatAnnotations(field);
      }

      Method[] linksMethods = getLinksMethod(type);
      if (linksMethods != null) {
        for (Method linksMethod : linksMethods) {
          if (!checkIsValidNewLinksMethod(linksMethod)) {
            throw new InvalidExtractorException(
                "Invalid links method " + linksMethod.getName() + " on class " + type.getName());
          }
        }
      }

      // check @Link
      Link[] links = type.getAnnotationsByType(Link.class);
      for (Link link : links) {
        checkIsValidLink(type, link);
      }

      VALID_EXTRACTOR_CLS.add(type);
    } else {
      throw new InvalidExtractorException(
          String.format("Class [%s] must has annotation @Extractor", type.getName()));
    }
  }

  private static void checkIsValidLink(Class<?> clz, Link link) {
    if (link.url().length <= 0
        && link.regexSelectors().length <= 0
        && link.jsonSelectors().length <= 0
        && link.cssSelectors().length <= 0
        && link.xpathSelectors().length <= 0) {
      throw new InvalidExtractorException(
          "Invalid @Link annotation, url or selectors must be specified");
    }
    for (Prop prop : ArrayUtil.addAll(link.attrs(), link.headers(), link.params())) {
      checkIsValidProp(clz, prop);
    }
  }

  private static void checkIsValidProp(Class<?> clz, Prop prop) {
    if (StrUtil.isNotBlank(prop.field())) {
      Field field = ReflectUtil.getField(clz, prop.field());
      if (field == null) {
        throw new InvalidExtractorException(
            "Invalid @Prop annotation, filed "
                + prop.field()
                + " not found on Class "
                + clz.getName());
      }
      Class<?> filedType = TypeUtil.getClass(TypeUtil.getType(field));
      if (!ClassUtil.isBasicType(filedType)) {
        throw new InvalidExtractorException(
            "Invalid @Prop annotation, field type "
                + filedType.getName()
                + " not supported, only support basic type");
      }
    }
  }

  private static void checkIsValidBodyField(Field field, Class<?> fieldType) {
    Body body = field.getAnnotation(Body.class);
    if (body != null) {
      if (fieldType.isArray()) {
        Class<?> componentType = fieldType.getComponentType();
        if (!byte.class.equals(componentType)) {
          throw new InvalidExtractorException(
              "Invalid @Body type on field " + field.getName() + ", only byte array supported");
        }
      } else {
        throw new InvalidExtractorException(
            "Invalid @Body type on field "
                + field.getName()
                + ", only byte array or byte list supported");
      }
    }
  }

  private static void checkIsValidUrlField(Field field, Class<?> fieldType) {
    Url url = field.getAnnotation(Url.class);
    if (url != null && !Convertors.isConvertibleType(fieldType)) {
      throw new InvalidExtractorException("Invalid @Url type on field " + field.getName());
    }
  }

  private static void checkIsValidParamField(Field field, Class<?> fieldType) {
    Param param = field.getAnnotation(Param.class);
    if (param != null && !Convertors.isConvertibleType(fieldType)) {
      throw new InvalidExtractorException("Invalid @Param type on field " + field.getName());
    }
  }

  private static void checkIsValidAttrField(Field field, Class<?> fieldType) {
    Attr attr = field.getAnnotation(Attr.class);
    if (attr != null && !Convertors.isConvertibleType(fieldType)) {
      throw new InvalidExtractorException("Invalid @Attr type on field " + field.getName());
    }
  }

  private static void checkIsValidSelectorFiled(
      Field field, Class<?> fieldType, Class<?> type, Collection<Class<?>> checkedClasses) {
    if (Selectors.hasSelectorAnnotation(field)) {
      if (fieldType.isArray()) {
        if (!checkedClasses.contains(fieldType.getComponentType())
            && !checkIsValidClass(fieldType.getComponentType())) {
          throw new InvalidExtractorException(
              "Unsupported type " + fieldType.getComponentType().getName());
        }
      } else if (Collection.class.isAssignableFrom(fieldType)) {
        if (Collection.class.equals(fieldType)
            || List.class.equals(fieldType)
            || Set.class.equals(fieldType)) {
          Class<?> actualType = getActualClass(field);
          if (actualType == null) {
            throw new InvalidExtractorException(
                "Can not get actual type of filed " + field.getName() + " on class " + type);
          }
          if (!checkedClasses.contains(actualType) && !checkIsValidClass(actualType)) {
            throw new InvalidExtractorException("Unsupported type " + actualType.getName());
          }

        } else {
          throw new InvalidExtractorException(
              "Unsupported collection type "
                  + fieldType.getName()
                  + ", only java.lang.Collection, java.lang.List or java.lang.Set supported");
        }
      } else if (!checkedClasses.contains(fieldType) && !checkIsValidClass(fieldType)) {
        throw new InvalidExtractorException("Unsupported type " + fieldType.getName());
      }
    }
  }

  private static void checkIsValidFormatAnnotations(Field field) {
    List<Annotation> multiLineAnnotations = Formatters.getMultiLineFormatAnnotations(field);
    if (multiLineAnnotations.size() > 1) {
      throw new InvalidExtractorException(
          "Filed " + field + " has " + multiLineAnnotations.size() + " line format annotations");
    }
  }

  private static boolean checkIsValidNewLinksMethod(@NonNull Method method) {
    Class<?> returnType = method.getReturnType();
    if (!String.class.isAssignableFrom(returnType)
        && !Request.class.isAssignableFrom(returnType)
        && !Collection.class.isAssignableFrom(returnType)
        && !returnType.isArray()) {
      return false;
    }
    if (returnType.isArray()
        && (!Request.class.isAssignableFrom(returnType.getComponentType())
            && !String.class.isAssignableFrom(returnType.getComponentType()))) {
      return false;
    } else if (Collection.class.isAssignableFrom(returnType)) {
      Type argType = TypeUtil.getTypeArgument(returnType, 0);
      Type argActualType = TypeUtil.getActualType(method.getGenericReturnType(), argType);
      Class<?> argActualCls = TypeUtil.getClass(argActualType);

      if (argActualCls == null
          || (!Request.class.isAssignableFrom(argActualCls)
              && !String.class.isAssignableFrom(argActualCls))) {
        return false;
      }
    }
    Class<?>[] paramTypes = method.getParameterTypes();
    if (paramTypes.length > 1) {
      return false;
    }
    return paramTypes.length == 0
        || Arrays.stream(paramTypes).allMatch(Response.class::isAssignableFrom);
  }

  @SuppressWarnings("unchecked")
  private static List<Request> invokeNewLinksMethod(
      Object targetObj, Method method, Response response) {
    Class<?>[] paramTypes = method.getParameterTypes();
    Object returnObj = null;
    if (paramTypes.length == 0) {
      returnObj = ReflectUtil.invoke(targetObj, method);
    } else if (paramTypes.length == 1) {
      returnObj = ReflectUtil.invoke(targetObj, method, response);
    }

    List<Object> objects = new ArrayList<>();
    List<Request> requests = new ArrayList<>();
    if (returnObj != null) {
      if (returnObj instanceof String || returnObj instanceof Request) {
        objects.add(returnObj);
      } else if (ArrayUtil.isArray(returnObj)) {
        int length = Array.getLength(returnObj);
        for (int i = 0; i < length; i++) {
          objects.add(Array.get(returnObj, i));
        }
      } else if (Collection.class.isAssignableFrom(returnObj.getClass())) {
        objects = ListUtil.toList(((Collection<Object>) returnObj).iterator());
      }

      objects.forEach(
          item -> {
            if (item instanceof String) {
              String u = item.toString();
              requests.add(Request.get(completeUrl(response.getRequest().getUrl(), u)));
            } else if (item instanceof Request) {
              requests.add((Request) item);
            }
          });
    }
    return requests;
  }

  public static Matcher extractMatcher(Class<?> extractorClass) {
    Extractor extractor = extractorClass.getAnnotation(Extractor.class);
    if (extractor.matcher().length > 0) {
      return Matchers.and(
          Arrays.stream(extractor.matcher()).map(m -> m.type().resolve(m)).toArray(Matcher[]::new));
    }
    return Matchers.ALL;
  }

  public static <T> Result<T> extract(Response response, Class<T> extractorClass) throws Exception {
    return extract(response.asText(), extractorClass, response);
  }

  @SuppressWarnings("unchecked")
  private static <T> Result<T> extract(String content, Class<T> extractorClass, Response response) {
    if (StrUtil.isBlank(content) || !checkIsValidExtractorClass(extractorClass)) {
      return new Result<>();
    }
    T t = ReflectUtil.newInstance(extractorClass);
    List<Request> requests = new ArrayList<>();

    // 提取内容
    Field[] fields = extractorClass.getDeclaredFields();
    for (Field field : fields) {
      if (field.getAnnotation(Body.class) != null) {
        ReflectUtil.setFieldValue(t, field, response.getBody());
        continue;
      }

      List<String> selected = null;
      if (field.getAnnotation(Url.class) != null) {
        selected = ListUtil.toList(response.getRequest().getUrl());
      } else if (field.getAnnotation(Param.class) != null) {
        Param param = field.getAnnotation(Param.class);
        CharSequence paramValue =
            UrlBuilder.of(response.getRequest().getUrl()).getQuery().get(param.name());
        if (paramValue != null) {
          selected = ListUtil.toList(paramValue.toString());
        } else {
          selected = ListUtil.toList(param.def());
        }
      } else if (field.getAnnotation(Attr.class) != null) {
        Attr attr = field.getAnnotation(Attr.class);
        String attrVal = null;
        Object o = response.getRequest().getAttribute(attr.name());
        attrVal = o == null ? attr.def() : o.toString();
        if (attrVal != null) {
          selected = ListUtil.toList(attrVal);
        }
      } else if (Selectors.hasSelectorAnnotation(field)) {
        selected = Selectors.select(content, field, response);
      }
      if (selected != null) {
        Object obj = convert(field, selected, requests, response);
        if (obj != null) {
          ReflectUtil.setFieldValue(t, field, obj);
        }
      }
    }

    // 提取链接
    Link[] links = extractorClass.getAnnotationsByType(Link.class);
    for (Link link : links) {
      requests.addAll(parseLinks(t, content, link, response));
    }

    // 提取自定义链接
    Method[] linksMethods = getLinksMethod(extractorClass);
    if (linksMethods != null) {
      for (Method linksMethod : linksMethods) {
        List<Request> newRequests = invokeNewLinksMethod(t, linksMethod, response);
        requests.addAll(newRequests);
      }
    }
    return new Result<T>(t, requests);
  }

  private static Method[] getLinksMethod(Class<?> extractorClass) {
    return ReflectUtil.getMethods(
        extractorClass, method -> method.isAnnotationPresent(LinkMethod.class));
  }

  private static List<Request> parseLinks(Object t, String content, Link link, Response response) {
    List<Request> requests = new ArrayList<>();
    List<String> urls = new ArrayList<>(ListUtil.toList(link.url()));

    List<Annotation> selectorAnnotations = new ArrayList<>();
    selectorAnnotations.addAll(ListUtil.toList(link.cssSelectors()));
    selectorAnnotations.addAll(ListUtil.toList(link.xpathSelectors()));
    selectorAnnotations.addAll(ListUtil.toList(link.jsonSelectors()));
    selectorAnnotations.addAll(ListUtil.toList(link.regexSelectors()));
    if (!selectorAnnotations.isEmpty()) {
      for (Annotation selectorAnnotation : selectorAnnotations) {
        List<String> selected = Selectors.select(content, selectorAnnotation, response);
        if (selected != null && !selected.isEmpty()) {
          urls.addAll(selected);
        }
      }
    }
    RegexFormatter[] formats = link.formats();
    for (String url : urls) {
      url = Formatters.format(url, formats, response);
      if (StrUtil.isNotBlank(url)) {
        Request request =
            new Request(completeUrl(response.getRequest().getUrl(), url), link.method())
                .setPriority(link.priority())
                .setRepeatable(link.repeatable());
        Arrays.stream(link.headers())
            .forEach(p -> request.addHeader(p.name(), resolveValueFromProp(t, p)));
        Arrays.stream(link.params())
            .forEach(p -> request.addParam(p.name(), resolveValueFromProp(t, p)));
        Arrays.stream(link.attrs())
            .forEach(p -> request.putAttribute(p.name(), resolveValueFromProp(t, p)));
        request.setInherit(link.inherit());
        requests.add(request);
      }
    }
    return requests;
  }

  private static String resolveValueFromProp(Object target, Prop prop) {
    if (StrUtil.isNotBlank(prop.field())) {
      Field field = ReflectUtil.getField(target.getClass(), prop.field());
      if (field == null) {
        throw new InvalidExtractorException(
            "can not found field " + prop.field() + " on class " + target.getClass());
      }
      Object val = ReflectUtil.getFieldValue(target, field);

      return val == null ? null : val.toString();
    }
    return prop.value();
  }

  private static String completeUrl(String currentUrl, String url) {
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

  private static Object convert(
      Field field, List<String> selected, List<Request> requests, Response response) {
    Class<?> fieldType = TypeUtil.getClass(field);
    if (fieldType.isArray() || Collection.class.isAssignableFrom(fieldType)) {
      Class<?> componentType =
          fieldType.isArray() ? fieldType.getComponentType() : getActualClass(field);
      if (componentType != null) {
        List<Object> list = new ArrayList<>();
        if (Convertors.isConvertibleType(componentType)) {
          for (String content : selected) {
            List<String> formatted = format(content, field, response);
            for (String s : formatted) {
              list.add(Convertors.convert(componentType, s, field, response));
            }
          }
        } else if (componentType.isAnnotationPresent(Extractor.class)) {
          for (String content : selected) {
            List<String> formatted = format(content, field, response);
            for (String s : formatted) {
              Result<?> result = extract(s, componentType, response);
              if (result.getObj() != null) {
                list.add(result.getObj());
              }
              if (result.getRequests() != null) {
                requests.addAll(result.getRequests());
              }
            }
          }
        }
        return fieldType.isArray()
            ? list.toArray()
            : (Set.class.equals(fieldType) ? new HashSet<>(list) : list);
      }
    } else if (Convertors.isConvertibleType(fieldType) && !selected.isEmpty()) {
      List<String> formatted = format(selected.get(0), field, response);
      return formatted.isEmpty()
          ? null
          : Convertors.convert(fieldType, formatted.get(0), field, response);
    } else if (fieldType.isAnnotationPresent(Extractor.class) && !selected.isEmpty()) {
      Result<?> result = extract(selected.get(0), fieldType, response);
      if (result.getRequests() != null) {
        requests.addAll(result.getRequests());
      }
      return result.getObj();
    }
    return null;
  }

  private static List<String> format(String content, Field field, Response response) {
    List<Annotation> multiLineFormats = Formatters.getMultiLineFormatAnnotations(field);
    if (multiLineFormats.isEmpty()) {
      return ListUtil.toList(Formatters.format(content, field, response));
    }
    List<String> formatted = Formatters.multiLineFormat(content, field);
    if (formatted == null || formatted.isEmpty()) {
      return Collections.emptyList();
    }
    return formatted.stream()
        .map(s -> Formatters.format(s, field, response))
        .collect(Collectors.toList());
  }
}
