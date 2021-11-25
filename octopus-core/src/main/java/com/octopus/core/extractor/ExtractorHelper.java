package com.octopus.core.extractor;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.Request;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.converter.BooleanConverter;
import com.octopus.core.extractor.converter.DateConverter;
import com.octopus.core.extractor.converter.DoubleConverter;
import com.octopus.core.extractor.converter.FloatConverter;
import com.octopus.core.extractor.converter.IntegerConverter;
import com.octopus.core.extractor.converter.LongConverter;
import com.octopus.core.extractor.converter.ShortConverter;
import com.octopus.core.extractor.converter.StringConverter;
import com.octopus.core.extractor.converter.TypeConverter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class ExtractorHelper {

  private static final Map<Class<?>, List<TypeConverter<?>>> CONVERTERS = new HashMap<>();

  static {
    registerTypeConverter(new IntegerConverter());
    registerTypeConverter(new LongConverter());
    registerTypeConverter(new ShortConverter());
    registerTypeConverter(new FloatConverter());
    registerTypeConverter(new DoubleConverter());
    registerTypeConverter(new BooleanConverter());
    registerTypeConverter(new ShortConverter());

    registerTypeConverter(new StringConverter());
    registerTypeConverter(new DateConverter());
  }

  public static void registerTypeConverter(@NonNull TypeConverter<?> converter) {
    Class<?>[] classes = converter.supportClasses();
    if (classes != null) {
      for (Class<?> cls : classes) {
        if (cls != null) {
          List<TypeConverter<?>> converters =
              CONVERTERS.computeIfAbsent(cls, k -> new ArrayList<>());
          converters.add(converter);
        }
      }
    }
  }

  private static boolean isConvertibleType(@NonNull Class<?> type) {
    return CONVERTERS.containsKey(type);
  }

  private static boolean checkIsValidClass(@NonNull Class<?> type) {
    return isConvertibleType(type) || checkIsValidExtractorClass(type);
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
          } else if (!isConvertibleType(fieldType)) {
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
        List<String> selected = SelectorHelper.selector(selector).select(content);
        Object obj = convert(field, selected, requests);
        ReflectUtil.setFieldValue(t, field, obj);
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
        if (isConvertibleType(componentType)) {
          for (String content : selected) {
            list.add(convert(componentType, content, fieldType.getAnnotations()));
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
    } else if (isConvertibleType(fieldType) && !selected.isEmpty()) {
      return convert(fieldType, selected.get(0), field.getAnnotations());
    } else if (fieldType.isAnnotationPresent(Extractor.class) && !selected.isEmpty()) {
      ExtractResult<?> extractResult = extract(selected.get(0), fieldType);
      if (extractResult.getRequests() != null) {
        requests.addAll(extractResult.getRequests());
      }
      return extractResult.getObj();
    }
    return null;
  }

  private static Object convert(Class<?> type, String content, Annotation[] annotations) {
    List<TypeConverter<?>> converters = CONVERTERS.get(type);
    for (TypeConverter<?> converter : converters) {
      Object converted = converter.convert(content, annotations);
      if (converted != null) {
        return converted;
      }
    }
    return null;
  }

  public static class ExtractResult<T> {
    private T obj;

    private List<Request> requests;

    public ExtractResult() {}

    public ExtractResult(T result, List<Request> requests) {
      this.obj = result;
      this.requests = requests;
    }

    public T getObj() {
      return obj;
    }

    public void setObj(T obj) {
      this.obj = obj;
    }

    public List<Request> getRequests() {
      return requests;
    }

    public void setRequests(List<Request> requests) {
      this.requests = requests;
    }
  }
}
