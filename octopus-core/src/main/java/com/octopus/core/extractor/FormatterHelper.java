package com.octopus.core.extractor;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.extractor.format.Formatter;
import com.octopus.core.extractor.format.RegexFormatter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public class FormatterHelper {
  private static final Map<Class<? extends Annotation>, Formatter<? extends Annotation>>
      FORMATTERS = new HashMap<>();

  static {
    registerFormatter(new RegexFormatter());
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

  public static String format(String val, Field field) {
    return format(val, field.getAnnotations());
  }

  public static String format(String val, Annotation annotation) {
    if (StrUtil.isNotBlank(val) && FORMATTERS.containsKey(annotation.annotationType())) {
      Formatter<? extends Annotation> formatter = FORMATTERS.get(annotation.annotationType());
      Method method =
          ReflectUtil.getMethod(formatter.getClass(), "format", String.class, Annotation.class);
      val = ReflectUtil.invoke(formatter, method, val, annotation);
    }
    return val;
  }

  public static String format(String val, Annotation[] annotations) {
    if (annotations != null) {
      for (Annotation annotation : annotations) {
        if (StrUtil.isNotBlank(val) && FORMATTERS.containsKey(annotation.annotationType())) {
          val = format(val, annotation);
        }
      }
    }
    return val;
  }
}
