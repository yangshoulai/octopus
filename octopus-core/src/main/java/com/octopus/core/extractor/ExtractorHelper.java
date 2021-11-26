package com.octopus.core.extractor;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.Request;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class ExtractorHelper {

  private static boolean checkIsValidClass(@NonNull Class<?> type) {
    return TypeConvertorHelper.isConvertibleType(type) || checkIsValidExtractorClass(type);
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
    if (type.isAnnotationPresent(Extractor.class)) {
      Field[] fields = type.getDeclaredFields();
      for (Field field : fields) {
        if (field.isAnnotationPresent(Selector.class)) {
          Class<?> fieldType = TypeUtil.getClass(field);
          if (fieldType.isArray()) {
            if (!checkIsValidClass(fieldType.getComponentType())) {
              return false;
            }
          } else if (Collection.class.isAssignableFrom(fieldType)) {
            if (Collection.class.equals(fieldType)
                || List.class.equals(fieldType)
                || Set.class.equals(fieldType)) {
              Class<?> actualType = getActualClass(field);
              if (actualType != null && checkIsValidClass(actualType)) {
                continue;
              }
            }
            return false;
          } else if (!TypeConvertorHelper.isConvertibleType(fieldType)) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }

  public static <T> ExtractResult<T> extract(String content, Class<T> extractorClass) {
    if (StrUtil.isBlank(content) || !checkIsValidExtractorClass(extractorClass)) {
      return new ExtractResult<>();
    }
    T t = ReflectUtil.newInstance(extractorClass);
    List<Request> requests = new ArrayList<>();
    Extractor extractor = extractorClass.getAnnotation(Extractor.class);

    // 提取链接
    Link[] links = extractor.links();
    if (links != null) {
      for (Link link : links) {
        requests.addAll(LinkHelper.parse(content, link));
      }
    }

    // 提取内容
    Field[] fields = extractorClass.getDeclaredFields();
    for (Field field : fields) {
      if (field.isAnnotationPresent(Selector.class)) {
        Selector selector = field.getAnnotation(Selector.class);
        List<String> selected = SelectorHelper.select(content, selector);
        Object obj = convert(field, selected, requests);
        if (obj != null) {
          ReflectUtil.setFieldValue(t, field, obj);
        }
      }
    }
    return new ExtractResult<T>(t, requests);
  }

  private static Object convert(Field field, List<String> selected, List<Request> requests) {
    Class<?> fieldType = TypeUtil.getClass(field);
    if (fieldType.isArray() || Collection.class.isAssignableFrom(fieldType)) {
      Class<?> componentType =
          fieldType.isArray() ? fieldType.getComponentType() : getActualClass(field);
      if (componentType != null) {
        List<Object> list = new ArrayList<>();
        if (TypeConvertorHelper.isConvertibleType(componentType)) {
          for (String content : selected) {
            content = FormatterHelper.format(content, field);
            list.add(TypeConvertorHelper.convert(componentType, content, field));
          }
        } else if (componentType.isAnnotationPresent(Extractor.class)) {
          for (String content : selected) {
            ExtractResult<?> extractResult = extract(content, componentType);
            if (extractResult.getObj() != null) {
              list.add(extractResult.getObj());
            }
            if (extractResult.getRequests() != null) {
              requests.addAll(extractResult.getRequests());
            }
          }
        }
        return fieldType.isArray()
            ? list.toArray()
            : (Set.class.equals(fieldType) ? new HashSet<>(list) : list);
      }
    } else if (TypeConvertorHelper.isConvertibleType(fieldType) && !selected.isEmpty()) {
      String content = FormatterHelper.format(selected.get(0), field);
      return TypeConvertorHelper.convert(fieldType, content, field);
    } else if (fieldType.isAnnotationPresent(Extractor.class) && !selected.isEmpty()) {
      ExtractResult<?> extractResult = extract(selected.get(0), fieldType);
      if (extractResult.getRequests() != null) {
        requests.addAll(extractResult.getRequests());
      }
      return extractResult.getObj();
    }
    return null;
  }
}
