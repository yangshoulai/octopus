package com.octopus.core.extractor;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.Request;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Links;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.format.RegexFormat;
import lombok.NonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class ExtractorHelper {

  private static final Set<Class<?>> VALID_EXTRACTOR_CLS = new HashSet<>();

  private static boolean checkIsValidClass(@NonNull Class<?> type) {
    return Convertors.isConvertibleType(type) || checkIsValidExtractorClass(type);
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
    checkIsValidExtractorClass(type, new HashSet<>());
    return true;
  }

  private static void checkIsValidExtractorClass(
      @NonNull Class<?> type, Collection<Class<?>> checkedClasses)
      throws InvalidExtractorException {
    if (VALID_EXTRACTOR_CLS.contains(type)) {
      return;
    }
    if (type.isAnnotationPresent(Extractor.class)) {
      checkedClasses.add(type);
      Field[] fields = type.getDeclaredFields();
      for (Field field : fields) {
        if (field.isAnnotationPresent(Selector.class)) {
          Class<?> fieldType = TypeUtil.getClass(field);
          if (fieldType.isArray()) {
            if (!checkedClasses.contains(fieldType.getComponentType())
                && !checkIsValidClass(fieldType.getComponentType())) {
              throw new InvalidExtractorException(
                  "Unsupported type " + fieldType.getComponentType().getName());
            }
          } else if (Collection.class.isAssignableFrom(fieldType)) {
            if (Collection.class.equals(fieldType)
                || List.class.equals(fieldType)
                || Set.class.equals(fieldType)) {
              Class<?> actualType = getActualClass(field);
              if (actualType == null) {
                throw new InvalidExtractorException(
                    "Can not get actual type of filed " + field.getName() + " on class " + type);
              }
              if (!checkedClasses.contains(actualType) && !checkIsValidClass(actualType)) {
                throw new InvalidExtractorException("Unsupported type " + actualType.getName());
              }

            } else {
              throw new InvalidExtractorException(
                  "Unsupported collection type "
                      + fieldType.getName()
                      + ", only java.lang.Collection, java.lang.List or java.lang.Set supported");
            }
          } else if (!checkedClasses.contains(fieldType) && !checkIsValidClass(fieldType)) {
            throw new InvalidExtractorException("Unsupported type " + fieldType.getName());
          }
        }
      }

      Method[] linksMethods = getLinksMethod(type);
      if (linksMethods != null) {
        for (Method linksMethod : linksMethods) {
          if (!checkIsValidNewLinksMethod(linksMethod)) {
            throw new InvalidExtractorException(
                "Invalid links method " + linksMethod.getName() + " on class " + type.getName());
          }
        }
      }
      VALID_EXTRACTOR_CLS.add(type);
    } else {
      throw new InvalidExtractorException(
          String.format("Class [%s] must has annotation @Extractor", type.getName()));
    }
  }

  private static boolean checkIsValidNewLinksMethod(@NonNull Method method) {
    Class<?> returnType = method.getReturnType();
    if (!String.class.isAssignableFrom(returnType)
        && !Request.class.isAssignableFrom(returnType)
        && !Collection.class.isAssignableFrom(returnType)
        && !returnType.isArray()) {
      return false;
    }
    if (returnType.isArray()
        && (!Request.class.isAssignableFrom(returnType.getComponentType())
            && !String.class.isAssignableFrom(returnType.getComponentType()))) {
      return false;
    } else if (Collection.class.isAssignableFrom(returnType)) {
      Type argType = TypeUtil.getTypeArgument(returnType, 0);
      Type argActualType = TypeUtil.getActualType(method.getGenericReturnType(), argType);
      Class<?> argActualCls = TypeUtil.getClass(argActualType);

      if (argActualCls == null
          || (!Request.class.isAssignableFrom(argActualCls)
              && !String.class.isAssignableFrom(argActualCls))) {
        return false;
      }
    }
    Class<?>[] paramTypes = method.getParameterTypes();
    if (paramTypes.length > 2) {
      return false;
    }
    return Arrays.stream(paramTypes).allMatch(String.class::isAssignableFrom);
  }

  @SuppressWarnings("unchecked")
  private static List<Request> invokeNewLinksMethod(
      Object targetObj, Method method, String url, String content) {
    Class<?>[] paramTypes = method.getParameterTypes();
    Object returnObj = null;
    if (paramTypes.length == 0) {
      returnObj = ReflectUtil.invoke(targetObj, method);
    } else if (paramTypes.length == 1) {
      returnObj = ReflectUtil.invoke(targetObj, method, url);
    } else if (paramTypes.length == 2) {
      returnObj = ReflectUtil.invoke(targetObj, method, url, content);
    }
    List<Request> requests = new ArrayList<>();
    if (returnObj != null) {
      if (returnObj instanceof String) {
        return ListUtil.toList(Request.get((String) returnObj));
      } else if (returnObj instanceof Request) {
        return ListUtil.toList((Request) returnObj);
      }
      List<Object> objects = new ArrayList<>();
      if (ArrayUtil.isArray(returnObj)) {
        int length = Array.getLength(returnObj);
        for (int i = 0; i < length; i++) {
          objects.add(Array.get(returnObj, i));
        }
      } else if (Collection.class.isAssignableFrom(returnObj.getClass())) {
        objects = ListUtil.toList(((Collection<Object>) returnObj).iterator());
      }

      objects.forEach(
          item -> {
            if (item instanceof String) {
              requests.add(Request.get((String) item));
            } else if (item instanceof Request) {
              requests.add((Request) item);
            }
          });
    }
    return requests;
  }

  @SuppressWarnings("unchecked")
  public static <T> Result<T> extract(String url, String content, Class<T> extractorClass) {
    if (StrUtil.isBlank(content) || !checkIsValidExtractorClass(extractorClass)) {
      return new Result<>();
    }
    T t = ReflectUtil.newInstance(extractorClass);
    List<Request> requests = new ArrayList<>();
    Extractor extractor = extractorClass.getAnnotation(Extractor.class);

    // 提取链接
    Link[] links = extractor.links();
    for (Link link : links) {
      requests.addAll(parseLinks(content, link));
    }

    // 提取内容
    Field[] fields = extractorClass.getDeclaredFields();
    for (Field field : fields) {
      if (field.isAnnotationPresent(Selector.class)) {
        Selector selector = field.getAnnotation(Selector.class);
        List<String> selected = Selectors.select(content, selector);
        Object obj = convert(url, field, selected, requests);
        if (obj != null) {
          ReflectUtil.setFieldValue(t, field, obj);
        }
      }
    }

    // 提取自定义链接
    Method[] linksMethods = getLinksMethod(extractorClass);
    if (linksMethods != null) {
      for (Method linksMethod : linksMethods) {
        List<Request> newRequests = invokeNewLinksMethod(t, linksMethod, url, content);
        requests.addAll(newRequests);
      }
    }
    return new Result<T>(t, requests);
  }

  private static Method[] getLinksMethod(Class<?> extractorClass) {
    return ReflectUtil.getMethods(
        extractorClass, method -> method.isAnnotationPresent(Links.class));
  }

  private static List<Request> parseLinks(String content, Link link) {
    List<Request> requests = new ArrayList<>();
    List<String> selected = Selectors.select(content, link.selector());
    if (selected != null && !selected.isEmpty()) {
      RegexFormat[] formats = link.formats();
      for (String url : selected) {
        url = Formatters.format(url, formats);
        if (StrUtil.isNotBlank(url)) {
          requests.add(
              new Request(url, link.method())
                  .setPriority(link.priority())
                  .setRepeatable(link.repeatable()));
        }
      }
    }
    return requests;
  }

  private static Object convert(
      String url, Field field, List<String> selected, List<Request> requests) {
    Class<?> fieldType = TypeUtil.getClass(field);
    if (fieldType.isArray() || Collection.class.isAssignableFrom(fieldType)) {
      Class<?> componentType =
          fieldType.isArray() ? fieldType.getComponentType() : getActualClass(field);
      if (componentType != null) {
        List<Object> list = new ArrayList<>();
        if (Convertors.isConvertibleType(componentType)) {
          for (String content : selected) {
            content = Formatters.format(content, field);
            list.add(Convertors.convert(componentType, content, field));
          }
        } else if (componentType.isAnnotationPresent(Extractor.class)) {
          for (String content : selected) {
            Result<?> result = extract(url, content, componentType);
            if (result.getObj() != null) {
              list.add(result.getObj());
            }
            if (result.getRequests() != null) {
              requests.addAll(result.getRequests());
            }
          }
        }
        return fieldType.isArray()
            ? list.toArray()
            : (Set.class.equals(fieldType) ? new HashSet<>(list) : list);
      }
    } else if (Convertors.isConvertibleType(fieldType) && !selected.isEmpty()) {
      String content = Formatters.format(selected.get(0), field);
      return Convertors.convert(fieldType, content, field);
    } else if (fieldType.isAnnotationPresent(Extractor.class) && !selected.isEmpty()) {
      Result<?> result = extract(url, selected.get(0), fieldType);
      if (result.getRequests() != null) {
        requests.addAll(result.getRequests());
      }
      return result.getObj();
    }
    return null;
  }
}
