package com.octopus.core.processor.extractor;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class ExtractorHelper {

  public static List<Field> getFieldsByAnnotation(
      Class<?> cls, Class<? extends Annotation> annotationType) {
    return Arrays.stream(ReflectUtil.getFields(cls, f -> f.isAnnotationPresent(annotationType)))
        .collect(Collectors.toList());
  }

  public static List<Method> getMethodsByAnnotation(
      Class<?> cls, Class<? extends Annotation> annotationType) {
    return Arrays.stream(ReflectUtil.getMethods(cls, f -> f.isAnnotationPresent(annotationType)))
        .collect(Collectors.toList());
  }

  public static Class<?> getCollectionComponentType(Type genericType) {
    ParameterizedType parameterizedType = TypeUtil.toParameterizedType(genericType);
    if (parameterizedType != null) {
      Type[] types = parameterizedType.getActualTypeArguments();
      if (types != null && types.length > 0) {
        return TypeUtil.getClass(types[0]);
      }
    }
    return null;
  }

  public static FieldInfo getFieldType(Field field) {
    FieldInfo fieldInfo = new FieldInfo();
    if (field.getType().isArray()) {
      fieldInfo.setArray(true);
      fieldInfo.setCollection(false);
      fieldInfo.setComponentClass(field.getType().getComponentType());
    } else if (Collection.class.isAssignableFrom(field.getType())) {
      fieldInfo.setArray(false);
      fieldInfo.setCollection(true);
      fieldInfo.setComponentClass(getCollectionComponentType(field.getGenericType()));
      fieldInfo.setCollectionClass(field.getType());
    } else {
      fieldInfo.setComponentClass(field.getType());
    }
    return fieldInfo;
  }
}
