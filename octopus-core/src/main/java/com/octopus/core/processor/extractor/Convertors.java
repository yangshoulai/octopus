package com.octopus.core.processor.extractor;

import cn.hutool.core.convert.BasicType;
import cn.hutool.core.util.ReflectUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.processor.extractor.convertor.BooleanConvertorHandler;
import com.octopus.core.processor.extractor.convertor.CharacterConvertorHandler;
import com.octopus.core.processor.extractor.convertor.ConvertorHandler;
import com.octopus.core.processor.extractor.convertor.DateConvertorHandler;
import com.octopus.core.processor.extractor.convertor.DoubleConvertorHandler;
import com.octopus.core.processor.extractor.convertor.FloatConvertorHandler;
import com.octopus.core.processor.extractor.convertor.IntegerConvertorHandler;
import com.octopus.core.processor.extractor.convertor.LongConvertorHandler;
import com.octopus.core.processor.extractor.convertor.ShortConvertorHandler;
import com.octopus.core.processor.extractor.convertor.StringConvertorHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class Convertors {

  private static final Map<Class<?>, ConvertorHandler<?, ? extends Annotation>> CONVERTERS =
      new HashMap<>();

  private static final Map<Class<? extends Annotation>, Annotation> DEFAULT_CONVERTER_ANNOTATIONS =
      new HashMap<>();

  static {
    registerTypeConvertor(new IntegerConvertorHandler());
    registerTypeConvertor(new LongConvertorHandler());
    registerTypeConvertor(new ShortConvertorHandler());
    registerTypeConvertor(new FloatConvertorHandler());
    registerTypeConvertor(new DoubleConvertorHandler());
    registerTypeConvertor(new BooleanConvertorHandler());
    registerTypeConvertor(new CharacterConvertorHandler());
    registerTypeConvertor(new StringConvertorHandler());
    registerTypeConvertor(new DateConvertorHandler());
  }

  public static void registerTypeConvertor(
      @NonNull ConvertorHandler<?, ? extends Annotation> converter) {
    Class<?> type = converter.getSupportedType();
    if (CONVERTERS.containsKey(type)) {
      throw new OctopusException("Convertor for type " + type + " already exist");
    }
    CONVERTERS.put(type, converter);
    try {
      Annotation annotation = newAnnotation(converter.getSupportedAnnotationType());
      DEFAULT_CONVERTER_ANNOTATIONS.put(converter.getSupportedAnnotationType(), annotation);
    } catch (ClassNotFoundException ignore) {
      //
    }
  }

  static ConvertorHandler<?, ? extends Annotation> getConvertor(@NonNull Class<?> type) {
    if (CONVERTERS.containsKey(type)) {
      return CONVERTERS.get(type);
    }
    if (type.isPrimitive() && CONVERTERS.containsKey(BasicType.wrap(type))) {
      return CONVERTERS.get(BasicType.wrap(type));
    }
    for (Entry<Class<?>, ConvertorHandler<?, ? extends Annotation>> entry : CONVERTERS.entrySet()) {
      if (type.isAssignableFrom(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  static boolean isConvertibleType(@NonNull Class<?> type) {
    if (CONVERTERS.containsKey(type)) {
      return true;
    }
    if (type.isPrimitive() && CONVERTERS.containsKey(BasicType.wrap(type))) {
      return true;
    }
    for (Entry<Class<?>, ConvertorHandler<?, ? extends Annotation>> entry : CONVERTERS.entrySet()) {
      if (type.isAssignableFrom(entry.getKey())) {
        return true;
      }
    }
    return false;
  }

  static Object convert(Class<?> type, String content, Field field, Response response) {
    ConvertorHandler<?, ? extends Annotation> handler = getConvertor(type);

    assert handler != null;
    Class<? extends Annotation> annotationClass = handler.getSupportedAnnotationType();
    Annotation annotation = field.getAnnotation(annotationClass);
    if (annotation == null) {
      annotation = DEFAULT_CONVERTER_ANNOTATIONS.get(annotationClass);
    }
    Method method =
        ReflectUtil.getMethod(
            handler.getClass(), "convert", String.class, annotationClass, Response.class);
    Object converted = ReflectUtil.invoke(handler, method, content, annotation, response);
    if (converted != null) {
      return converted;
    }
    return String.class.equals(type) ? content : null;
  }

  @SuppressWarnings("unchecked")
  static <A extends Annotation> A newAnnotation(Class<A> cls) throws ClassNotFoundException {
    Map<String, Object> params = new HashMap<>();
    Arrays.stream(ReflectUtil.getMethods(cls))
        .forEach(
            m -> {
              params.put(m.getName(), m.getDefaultValue());
            });

    InvocationHandler invocationHandler =
        (InvocationHandler)
            ReflectUtil.newInstance(
                Class.forName("sun.reflect.annotation.AnnotationInvocationHandler"), cls, params);
    return (A)
        Proxy.newProxyInstance(
            Convertors.class.getClassLoader(), new Class[] {cls}, invocationHandler);
  }
}
