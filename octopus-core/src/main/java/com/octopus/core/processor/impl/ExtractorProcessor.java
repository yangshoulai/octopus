package com.octopus.core.processor.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.exception.InvalidExtractorException;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.*;
import com.octopus.core.processor.annotation.Converter;
import com.octopus.core.processor.annotation.Selector;
import com.octopus.core.processor.annotation.*;
import com.octopus.core.utils.AnnotationUtil;
import com.octopus.core.utils.RequestHelper;
import lombok.NonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

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
            ExtractorValidator.getInstance().validate(extractorClass);
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
                collector.collect(result.getObj(), response);
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
        Field[] fields = ReflectUtil.getFields(extractorClass);
        for (Field field : fields) {
            List<Selector> selectors = AnnotationUtil.getMergedAnnotations(field, Selector.class);
            if (selectors != null) {
                this.extractField(result, source, field, selectors, response);
            }
        }
        Link[] links = Objects.requireNonNull(AnnotationUtil.getDirectlyMergedAnnotation(extractorClass, Extractor.class)).links();
        for (Link link : links) {
            this.extractLink(result, link, source, response);
        }

        Method[] methods = ReflectUtil.getMethods(extractorClass, method -> method.isAnnotationPresent(LinkMethod.class));
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
                            result.getRequests().add(Request.get(RequestHelper.completeUrl(response.getRequest().getUrl(), u)));
                        } else if (item instanceof Request) {
                            result.getRequests().add((Request) item);
                        }
                    });
        }
    }

    private void extractLink(Result<?> result, Link link, String source, Response response) {
        Set<String> urls = new HashSet<>();
        if (StrUtil.isNotBlank(link.url())) {
            urls.add(link.url());
        }
        List<String> selected = SelectorHelper.getInstance().selectBySelectorAnnotation(link.selector(), source, true, response);
        if (selected != null) {
            urls.addAll(selected);
        }
        for (String url : urls) {
            if (StrUtil.isNotBlank(url)) {
                Request request =
                        new Request(RequestHelper.completeUrl(response.getRequest().getUrl(), url), link.method())
                                .setPriority(link.priority())
                                .setRepeatable(link.repeatable());
                Arrays.stream(link.headers())
                        .forEach(p -> request.addHeader(p.name(), resolveValueFromProp(source, result.getObj(), p, response)));
                Arrays.stream(link.params())
                        .forEach(p -> request.addParam(p.name(), resolveValueFromProp(source, result.getObj(), p, response)));
                Arrays.stream(link.attrs())
                        .forEach(p -> request.putAttribute(p.name(), resolveValueFromProp(source, result.getObj(), p, response)));
                request.setInherit(link.inherit());
                request.setCache(link.cache());
                result.getRequests().add(request);
            }
        }
    }

    private String resolveValueFromProp(String source, Object target, Prop prop, Response response) {
        String val = null;
        if (StrUtil.isNotBlank(prop.field())) {
            Field field = ReflectUtil.getField(target.getClass(), prop.field());
            Object v = ReflectUtil.getFieldValue(target, field);
            if (v != null) {
                val = v.toString();
            }
        }
        if (prop.selector() != null) {
            List<String> selected = SelectorHelper.getInstance().selectBySelectorAnnotation(prop.selector(), source, true, response);
            if (selected != null) {
                val = String.join(",", selected);
            }
        }
        return val;
    }


    @SuppressWarnings("unchecked")
    private void extractField(
            Result<?> result, String source, Field field, List<Selector> selectors, Response response) {
        FieldInfo fieldInfo = ExtractorHelper.getFieldType(field);
        boolean multi = fieldInfo.isArray() || fieldInfo.isCollection();
        List<String> selected = SelectorHelper.getInstance().selectBySelectorAnnotation(selectors, source, multi, response);
        if (selected != null && !selected.isEmpty()) {
            Converter annotation = AnnotationUtil.getDirectlyMergedAnnotation(field, Converter.class);
            if (ConverterRegistry.getInstance().isSupportType(field.getType())) {
                ReflectUtil.setFieldValue(result.getObj(), field, ConverterRegistry.getInstance().convert(selected.get(0), field.getType(), annotation));
                return;
            }
            if (fieldInfo.isArray() || fieldInfo.isCollection()) {
                List<Object> list = new ArrayList<>();
                Class<?> componentClass = fieldInfo.getComponentClass();
                for (String item : selected) {
                    if (!(componentClass.isAnnotationPresent(Extractor.class))) {
                        Object obj = ConverterRegistry.getInstance().convert(item, componentClass, annotation);
                        list.add(obj);
                    } else {
                        Result<?> r = extract(item, response, componentClass);
                        if (r.getRequests() != null) {
                            result.getRequests().addAll(r.getRequests());
                        }
                        list.add(r.getObj());
                    }
                }

                if (fieldInfo.isArray()) {
                    ReflectUtil.setFieldValue(result.getObj(), field, list.toArray());
                } else {
                    Class<?> collectionClass = ConverterRegistry.getInstance().getCollectionImplClass(fieldInfo.getCollectionClass());
                    Collection<Object> collection =
                            (Collection<Object>) ReflectUtil.newInstance(collectionClass);
                    collection.addAll(list);
                    ReflectUtil.setFieldValue(result.getObj(), field, collection);
                }

            } else if (fieldInfo.getComponentClass().isAnnotationPresent(Extractor.class)) {
                Result<?> r = extract(selected.get(0), response, fieldInfo.getComponentClass());
                if (r.getRequests() != null) {
                    result.getRequests().addAll(r.getRequests());
                }
                ReflectUtil.setFieldValue(result.getObj(), field, r.getObj());
            } else {
                Object obj = ConverterRegistry.getInstance().convert(selected.get(0), fieldInfo.getComponentClass(), annotation);
                ReflectUtil.setFieldValue(result.getObj(), field, obj);
            }
        }
    }
}
