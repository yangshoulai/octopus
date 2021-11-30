package com.octopus.core.extractor;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.extractor.convertor.BooleanConvertor;
import com.octopus.core.extractor.convertor.DateConvertor;
import com.octopus.core.extractor.convertor.DoubleConvertor;
import com.octopus.core.extractor.convertor.FloatConvertor;
import com.octopus.core.extractor.convertor.IntegerConvertor;
import com.octopus.core.extractor.convertor.LongConvertor;
import com.octopus.core.extractor.convertor.ShortConvertor;
import com.octopus.core.extractor.convertor.StringConvertor;
import com.octopus.core.extractor.convertor.Convertor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

  private static final Map<Class<?>, List<Convertor<?, ? extends Annotation>>> CONVERTERS =
      new HashMap<>();

  static {
    registerTypeConvertor(new IntegerConvertor());
    registerTypeConvertor(new LongConvertor());
    registerTypeConvertor(new ShortConvertor());
    registerTypeConvertor(new FloatConvertor());
    registerTypeConvertor(new DoubleConvertor());
    registerTypeConvertor(new BooleanConvertor());
    registerTypeConvertor(new ShortConvertor());

    registerTypeConvertor(new StringConvertor());
    registerTypeConvertor(new DateConvertor());
  }

  public static void registerTypeConvertor(@NonNull Convertor<?, ? extends Annotation> converter) {
    Class<?>[] classes = converter.getSupportClasses();
    if (classes != null) {
      for (Class<?> cls : classes) {
        if (cls != null) {
          List<Convertor<?, ? extends Annotation>> converters =
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
  static Object convert(Class<?> type, String content, Field field) {
    Object converted = null;
    List<Convertor<?, ? extends Annotation>> converters = CONVERTERS.get(type);
    for (Convertor<?, ? extends Annotation> converter : converters) {
      Annotation annotation = null;
      Type[] types = TypeUtil.getTypeArguments(converter.getClass());
      if (types != null && types.length == 2) {
        annotation = field.getAnnotation((Class<? extends Annotation>) TypeUtil.getClass(types[1]));
      }
      converted =
          ReflectUtil.invoke(
              converter,
              ReflectUtil.getMethod(
                  converter.getClass(), "convert", String.class, Annotation.class),
              content,
              annotation);
      if (converted != null) {
        return converted;
      }
    }
    return null;
  }
}
