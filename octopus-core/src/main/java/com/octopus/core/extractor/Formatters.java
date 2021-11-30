package com.octopus.core.extractor;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.extractor.format.Formatter;
import com.octopus.core.extractor.format.MultiLineFormatter;
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.extractor.format.SplitFormatter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public class Formatters {
  private static final Map<Class<? extends Annotation>, Formatter<? extends Annotation>>
      FORMATTERS = new HashMap<>();

  private static final Map<Class<? extends Annotation>, MultiLineFormatter<? extends Annotation>>
      MULTI_LINE_FORMATTERS = new HashMap<>();

  static {
    registerFormatter(new RegexFormatter());
    registerFormatter(new SplitFormatter());
  }

  @SuppressWarnings("unchecked")
  public static void registerFormatter(@NonNull Formatter<? extends Annotation> formatter) {
    Type[] types = TypeUtil.getTypeArguments(formatter.getClass());
    if (types != null && types.length == 1) {
      if (FORMATTERS.containsKey(TypeUtil.getClass(types[0]))) {
        throw new OctopusException("Formatter for annotation " + types[0] + " already exists");
      } else {
        FORMATTERS.put((Class<? extends Annotation>) TypeUtil.getClass(types[0]), formatter);
      }
    } else {
      throw new OctopusException("Not a valid formatter");
    }
  }

  @SuppressWarnings("unchecked")
  public static void registerFormatter(
      @NonNull MultiLineFormatter<? extends Annotation> formatter) {
    Type[] types = TypeUtil.getTypeArguments(formatter.getClass());
    if (types != null && types.length == 1) {
      if (MULTI_LINE_FORMATTERS.containsKey(TypeUtil.getClass(types[0]))) {
        throw new OctopusException("Formatter for annotation " + types[0] + " already exists");
      } else {
        MULTI_LINE_FORMATTERS.put(
            (Class<? extends Annotation>) TypeUtil.getClass(types[0]), formatter);
      }
    } else {
      throw new OctopusException("Not a valid formatter");
    }
  }

  static String format(String val, Field field) {
    return format(val, field.getAnnotations());
  }

  static String format(String val, Annotation annotation) {
    if (StrUtil.isNotBlank(val) && FORMATTERS.containsKey(annotation.annotationType())) {
      Formatter<? extends Annotation> formatter = FORMATTERS.get(annotation.annotationType());
      Method method =
          ReflectUtil.getMethod(formatter.getClass(), "format", String.class, Annotation.class);
      val = ReflectUtil.invoke(formatter, method, val, annotation);
    }
    return val;
  }

  static String format(String val, Annotation[] annotations) {
    if (annotations != null) {
      for (Annotation annotation : annotations) {
        if (StrUtil.isNotBlank(val) && FORMATTERS.containsKey(annotation.annotationType())) {
          val = format(val, annotation);
        }
      }
    }
    return val;
  }

  static List<Annotation> getMultiLineFormatAnnotations(Field field) {
    List<Annotation> formatAnnotations = new ArrayList<>();
    Annotation[] annotations = field.getAnnotations();
    for (Annotation annotation : annotations) {
      if (MULTI_LINE_FORMATTERS.containsKey(annotation.annotationType())) {
        formatAnnotations.add(annotation);
      }
    }
    return formatAnnotations;
  }

  static List<String> multiLineFormat(String val, Field field) {
    List<Annotation> multiLineAnnotations = getMultiLineFormatAnnotations(field);
    if (!multiLineAnnotations.isEmpty()) {
      Annotation annotation = multiLineAnnotations.get(0);
      MultiLineFormatter<? extends Annotation> formatter =
          MULTI_LINE_FORMATTERS.get(annotation.annotationType());
      Method method =
          ReflectUtil.getMethod(formatter.getClass(), "format", String.class, Annotation.class);
      return ReflectUtil.invoke(formatter, method, val, annotation);
    }

    return Collections.emptyList();
  }
}
