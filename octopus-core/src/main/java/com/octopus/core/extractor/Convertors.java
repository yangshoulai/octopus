package com.octopus.core.extractor;

import cn.hutool.core.convert.BasicType;
import cn.hutool.core.util.ReflectUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.extractor.convertor.BooleanConvertorHandler;
import com.octopus.core.extractor.convertor.CharacterConvertorHandler;
import com.octopus.core.extractor.convertor.ConvertorHandler;
import com.octopus.core.extractor.convertor.DateConvertorHandler;
import com.octopus.core.extractor.convertor.DoubleConvertorHandler;
import com.octopus.core.extractor.convertor.FloatConvertorHandler;
import com.octopus.core.extractor.convertor.IntegerConvertorHandler;
import com.octopus.core.extractor.convertor.LongConvertorHandler;
import com.octopus.core.extractor.convertor.ShortConvertorHandler;
import com.octopus.core.extractor.convertor.StringConvertorHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    Object converted = null;
    ConvertorHandler<?, ? extends Annotation> handler = getConvertor(type);

    assert handler != null;
    Class<? extends Annotation> annotationClass = handler.getSupportedAnnotationType();
    Annotation annotation = field.getAnnotation(annotationClass);
    Method method =
        ReflectUtil.getMethod(
            handler.getClass(), "convert", String.class, annotationClass, Response.class);
    converted = ReflectUtil.invoke(handler, method, content, annotation, response);
    if (converted != null) {
      return converted;
    }
    return String.class.equals(type) ? content : null;
  }
}
