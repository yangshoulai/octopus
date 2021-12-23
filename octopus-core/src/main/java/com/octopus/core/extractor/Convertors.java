package com.octopus.core.extractor;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.Response;
import com.octopus.core.extractor.convertor.BooleanConvertorHandler;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class Convertors {

  private static final Map<Class<?>, List<ConvertorHandler<?, ? extends Annotation>>> CONVERTERS =
      new HashMap<>();

  static {
    registerTypeConvertor(new IntegerConvertorHandler());
    registerTypeConvertor(new LongConvertorHandler());
    registerTypeConvertor(new ShortConvertorHandler());
    registerTypeConvertor(new FloatConvertorHandler());
    registerTypeConvertor(new DoubleConvertorHandler());
    registerTypeConvertor(new BooleanConvertorHandler());
    registerTypeConvertor(new ShortConvertorHandler());

    registerTypeConvertor(new StringConvertorHandler());
    registerTypeConvertor(new DateConvertorHandler());
  }

  public static void registerTypeConvertor(
      @NonNull ConvertorHandler<?, ? extends Annotation> converter) {
    Class<?>[] classes = converter.getSupportClasses();
    if (classes != null) {
      for (Class<?> cls : classes) {
        if (cls != null) {
          List<ConvertorHandler<?, ? extends Annotation>> converters =
              CONVERTERS.computeIfAbsent(cls, k -> new ArrayList<>());
          converters.add(converter);
        }
      }
    }
  }

  static boolean isConvertibleType(@NonNull Class<?> type) {
    return CONVERTERS.containsKey(type);
  }

  @SuppressWarnings("unchecked")
  static Object convert(Class<?> type, String content, Field field, Response response) {
    Object converted = null;
    List<ConvertorHandler<?, ? extends Annotation>> converters = CONVERTERS.get(type);
    for (ConvertorHandler<?, ? extends Annotation> converter : converters) {
      Annotation annotation = null;
      Class<? extends Annotation> annotationClass = null;
      Type[] types = TypeUtil.getTypeArguments(converter.getClass());
      if (types != null && types.length == 2) {
        annotationClass = (Class<? extends Annotation>) TypeUtil.getClass(types[1]);
        annotation = field.getAnnotation(annotationClass);
      }
      if (annotationClass != null) {
        Method method =
            ReflectUtil.getMethod(
                converter.getClass(), "convert", String.class, annotationClass, Response.class);
        converted = ReflectUtil.invoke(converter, method, content, annotation, response);
        if (converted != null) {
          return converted;
        }
      }
    }
    return content;
  }
}
