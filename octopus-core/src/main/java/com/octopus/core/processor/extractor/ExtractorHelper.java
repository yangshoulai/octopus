package com.octopus.core.processor.extractor;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.annotation.Body;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.LinkMethod;
import com.octopus.core.processor.extractor.annotation.Prop;
import com.octopus.core.processor.extractor.format.RegexFormatter;
import com.octopus.core.processor.matcher.Matcher;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class ExtractorHelper {

  private static final Set<String> VALID_EXTRACTOR_CLS = new HashSet<>();

  private static final Map<Class<? extends Collection>, Class<? extends Collection>>
      VALID_COLLECTION_MAP = new HashMap<>();

  static {
    VALID_COLLECTION_MAP.put(Collection.class, ArrayList.class);
    VALID_COLLECTION_MAP.put(List.class, ArrayList.class);
    VALID_COLLECTION_MAP.put(ArrayList.class, ArrayList.class);
    VALID_COLLECTION_MAP.put(LinkedList.class, ArrayList.class);
    VALID_COLLECTION_MAP.put(Set.class, HashSet.class);
    VALID_COLLECTION_MAP.put(HashSet.class, HashSet.class);
    VALID_COLLECTION_MAP.put(LinkedHashSet.class, LinkedHashSet.class);
  }

  private static boolean isValidCollectionType(Class<?> type) {

    return !Collection.class.isAssignableFrom(type) || VALID_COLLECTION_MAP.containsKey(type);
  }

  private static ConvertType getFieldConvertType(Type type, Field field) {
    ConvertType convertType = new ConvertType();
    Type actualType = TypeUtil.getActualType(type, field);
    Class<?> clz = TypeUtil.getClass(actualType);
    if (clz != null) {
      convertType.setArray(clz.isArray());
      convertType.setCollection(Collection.class.isAssignableFrom(clz));
      convertType.setActualType(actualType);
      if (clz.isArray()) {
        convertType.setComponentType(clz.getComponentType());
      } else if (Collection.class.isAssignableFrom(clz)) {
        Type[] types = TypeUtil.toParameterizedType(actualType).getActualTypeArguments();
        if (types != null && types.length > 0) {
          convertType.setActualType(types[0]);
          convertType.setComponentType(TypeUtil.getClass(types[0]));
        }
        convertType.setCollectionClass(clz);
      } else {
        convertType.setComponentType(clz);
      }
    } else if (actualType instanceof GenericArrayType) {
      convertType.setArray(true);
      actualType =
          TypeUtil.getActualType(type, ((GenericArrayType) actualType).getGenericComponentType());
      convertType.setActualType(actualType);
      convertType.setComponentType(TypeUtil.getClass(actualType));
    }
    return convertType;
  }

  public static boolean checkIsValidExtractorClass(@NonNull Class<?> type) {
    return checkIsValidExtractorClass(type, type);
  }

  public static boolean checkIsValidExtractorClass(
      @NonNull Class<?> type, @NonNull Type actualType) {
    checkIsValidExtractorClass(type, actualType, new HashSet<>());
    return true;
  }

  private static Field[] getFieldAnnotatedBySelector(Class<?> cls) {
    return ReflectUtil.getFields(
        cls, f -> f.getAnnotation(Body.class) != null || Selectors.hasSelectorAnnotation(f));
  }

  private static void checkIsValidExtractorClass(
      @NonNull Class<?> type, @NonNull Type actualType, Collection<String> checkedClasses)
      throws InvalidExtractorException {
    if (VALID_EXTRACTOR_CLS.contains(actualType.toString())) {
      return;
    }
    if (type.isAnnotationPresent(Extractor.class)) {
      checkedClasses.add(actualType.toString());
      Field[] fields = getFieldAnnotatedBySelector(type);
      for (Field field : fields) {
        ConvertType convertType = getFieldConvertType(actualType, field);
        if (convertType.getComponentType() == null) {
          throw new InvalidExtractorException(
              "Field type [" + type + "@" + field + "] is not convertible");
        }
        // check @Body
        checkIsValidBodyField(field, convertType);

        // check Selector annotations
        checkIsValidSelectorFiled(field, convertType, type, checkedClasses);

        // check format annotations
        checkIsValidFormatAnnotations(field);
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

      // check @Link
      Link[] links = type.getAnnotationsByType(Link.class);
      for (Link link : links) {
        checkIsValidLink(type, link);
      }

      VALID_EXTRACTOR_CLS.add(actualType.toString());
    } else {
      throw new InvalidExtractorException(
          String.format("Class [%s] must has annotation @Extractor", type.getName()));
    }
  }

  private static void checkIsValidLink(Class<?> clz, Link link) {
    if (link.url().length <= 0
        && link.regexSelectors().length <= 0
        && link.jsonSelectors().length <= 0
        && link.cssSelectors().length <= 0
        && link.xpathSelectors().length <= 0) {
      throw new InvalidExtractorException(
          "Invalid @Link annotation, url or selectors must be specified");
    }
    for (Prop prop : ArrayUtil.addAll(link.attrs(), link.headers(), link.params())) {
      checkIsValidProp(clz, prop);
    }
  }

  private static void checkIsValidProp(Class<?> clz, Prop prop) {
    if (StrUtil.isNotBlank(prop.field())) {
      Field field = ReflectUtil.getField(clz, prop.field());
      if (field == null) {
        throw new InvalidExtractorException(
            "Invalid @Prop annotation, filed "
                + prop.field()
                + " not found on Class "
                + clz.getName());
      }
      Class<?> filedType = TypeUtil.getClass(TypeUtil.getType(field));
      if (!CharSequence.class.isAssignableFrom(filedType) && !ClassUtil.isBasicType(filedType)) {
        throw new InvalidExtractorException(
            "Invalid @Prop annotation, field type "
                + filedType.getName()
                + " not supported, only support basic type or String");
      }
    }
  }

  private static void checkIsValidBodyField(Field field, ConvertType convertType) {
    Body body = field.getAnnotation(Body.class);
    if (body != null) {
      if (!convertType.isArray() || !byte.class.equals(convertType.getComponentType())) {
        throw new InvalidExtractorException(
            "@Body annotation only supported byte array type, e.g. @Body byte[] body;");
      }
    }
  }

  private static void checkIsValidSelectorFiled(
      Field field, ConvertType convertType, Class<?> type, Collection<String> checkedClasses) {
    if (Selectors.hasSelectorAnnotation(field)) {
      if (Selectors.getSelectorAnnotations(field).size() > 1) {
        throw new InvalidExtractorException("Multi selectors found on class " + type);
      }
      if (convertType.isCollection() && !isValidCollectionType(convertType.getCollectionClass())) {
        throw new InvalidExtractorException(
            "Not supported collection type "
                + convertType.getCollectionClass()
                + " on field "
                + field
                + " in class "
                + type);
      }
      Class<?> componentType = convertType.getComponentType();

      if (!checkedClasses.contains(componentType) && !Convertors.isConvertibleType(componentType)) {
        checkIsValidExtractorClass(componentType, convertType.getActualType(), checkedClasses);
      }
    }
  }

  private static void checkIsValidFormatAnnotations(Field field) {
    List<Annotation> multiLineAnnotations = Formatters.getMultiLineFormatAnnotations(field);
    if (multiLineAnnotations.size() > 1) {
      throw new InvalidExtractorException(
          "Filed " + field + " has " + multiLineAnnotations.size() + " line format annotations");
    }
  }

  private static boolean checkIsValidNewLinksMethod(@NonNull Method method) {
    Class<?> returnType = method.getReturnType();
    if (!String.class.isAssignableFrom(returnType)
        && !Request.class.equals(returnType)
        && !isValidCollectionType(returnType)
        && !returnType.isArray()) {
      return false;
    }
    if (returnType.isArray()
        && (!Request.class.equals(returnType.getComponentType())
            && !String.class.equals(returnType.getComponentType()))) {
      return false;
    } else if (Collection.class.isAssignableFrom(returnType)) {
      Type argType = TypeUtil.getTypeArgument(returnType, 0);
      Type argActualType = TypeUtil.getActualType(method.getGenericReturnType(), argType);
      Class<?> argActualCls = TypeUtil.getClass(argActualType);
      if (argActualCls == null
          || (!Request.class.equals(argActualCls) && !String.class.equals(argActualCls))) {
        return false;
      }
    }
    Class<?>[] paramTypes = method.getParameterTypes();
    if (paramTypes.length > 1) {
      return false;
    }
    return paramTypes.length == 0 || Arrays.stream(paramTypes).allMatch(Response.class::equals);
  }

  @SuppressWarnings("unchecked")
  private static List<Request> invokeNewLinksMethod(
      Object targetObj, Method method, Response response) {
    Class<?>[] paramTypes = method.getParameterTypes();
    Object returnObj = null;
    if (paramTypes.length == 0) {
      returnObj = ReflectUtil.invoke(targetObj, method);
    } else if (paramTypes.length == 1) {
      returnObj = ReflectUtil.invoke(targetObj, method, response);
    }

    List<Object> objects = new ArrayList<>();
    List<Request> requests = new ArrayList<>();
    if (returnObj != null) {
      if (returnObj instanceof String || returnObj instanceof Request) {
        objects.add(returnObj);
      } else if (ArrayUtil.isArray(returnObj)) {
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
              String u = item.toString();
              requests.add(Request.get(completeUrl(response.getRequest().getUrl(), u)));
            } else if (item instanceof Request) {
              requests.add((Request) item);
            }
          });
    }
    return requests;
  }

  public static Matcher extractMatcher(Class<?> extractorClass) {
    Extractor extractor = extractorClass.getAnnotation(Extractor.class);
    if (extractor.matcher() != null) {
      return extractor.matcher().type().resolve(extractor.matcher());
    }
    return null;
  }

  public static <T> Result<T> extract(Response response, Class<T> extractorClass) {
    return extract(response.asText(), extractorClass, extractorClass, response);
  }

  private static <T> Result<T> extract(
      String content, Class<T> extractorClass, Type actualType, Response response) {
    if (StrUtil.isBlank(content)
        || !checkIsValidExtractorClass(extractorClass, Objects.requireNonNull(actualType))) {
      return new Result<>();
    }
    T t = ReflectUtil.newInstance(extractorClass);
    List<Request> requests = new ArrayList<>();

    // 提取内容
    Field[] fields =
        ReflectUtil.getFields(
            extractorClass,
            f -> f.getAnnotation(Body.class) != null || Selectors.hasSelectorAnnotation(f));
    for (Field field : fields) {
      if (field.getAnnotation(Body.class) != null) {
        ReflectUtil.setFieldValue(t, field, response.getBody());
        continue;
      }

      List<String> selected = null;
      if (Selectors.hasSelectorAnnotation(field)) {
        selected = Selectors.select(content, field, response);
      }
      if (selected != null) {
        ConvertType convertType = getFieldConvertType(actualType, field);
        Object obj = convert(field, convertType, selected, requests, response);
        if (obj != null) {
          ReflectUtil.setFieldValue(t, field, obj);
        }
      }
    }

    // 提取链接
    Link[] links = extractorClass.getAnnotationsByType(Link.class);
    for (Link link : links) {
      requests.addAll(parseLinks(t, content, link, response));
    }

    // 提取自定义链接
    Method[] linksMethods = getLinksMethod(extractorClass);
    if (linksMethods != null) {
      for (Method linksMethod : linksMethods) {
        List<Request> newRequests = invokeNewLinksMethod(t, linksMethod, response);
        requests.addAll(newRequests);
      }
    }
    return new Result<>(t, requests);
  }

  private static Method[] getLinksMethod(Class<?> extractorClass) {
    return ReflectUtil.getMethods(
        extractorClass, method -> method.isAnnotationPresent(LinkMethod.class));
  }

  private static List<Request> parseLinks(Object t, String content, Link link, Response response) {
    List<Request> requests = new ArrayList<>();
    List<String> urls = new ArrayList<>(ListUtil.toList(link.url()));

    List<Annotation> selectorAnnotations = new ArrayList<>();
    selectorAnnotations.addAll(ListUtil.toList(link.cssSelectors()));
    selectorAnnotations.addAll(ListUtil.toList(link.xpathSelectors()));
    selectorAnnotations.addAll(ListUtil.toList(link.jsonSelectors()));
    selectorAnnotations.addAll(ListUtil.toList(link.regexSelectors()));
    if (!selectorAnnotations.isEmpty()) {
      for (Annotation selectorAnnotation : selectorAnnotations) {
        List<String> selected = Selectors.select(content, selectorAnnotation, response);
        if (selected != null && !selected.isEmpty()) {
          urls.addAll(selected);
        }
      }
    }
    RegexFormatter[] formats = link.formats();
    for (String url : urls) {
      url = Formatters.format(url, formats, response);
      if (StrUtil.isNotBlank(url)) {
        Request request =
            new Request(completeUrl(response.getRequest().getUrl(), url), link.method())
                .setPriority(link.priority())
                .setRepeatable(link.repeatable());
        Arrays.stream(link.headers())
            .forEach(p -> request.addHeader(p.name(), resolveValueFromProp(t, p)));
        Arrays.stream(link.params())
            .forEach(p -> request.addParam(p.name(), resolveValueFromProp(t, p)));
        Arrays.stream(link.attrs())
            .forEach(p -> request.putAttribute(p.name(), resolveValueFromProp(t, p)));
        request.setInherit(link.inherit());
        requests.add(request);
      }
    }
    return requests;
  }

  private static String resolveValueFromProp(Object target, Prop prop) {
    if (StrUtil.isNotBlank(prop.field())) {
      Field field = ReflectUtil.getField(target.getClass(), prop.field());
      Object val = ReflectUtil.getFieldValue(target, field);
      return val == null ? null : val.toString();
    }
    return prop.value();
  }

  private static String completeUrl(String currentUrl, String url) {
    if (!HttpUtil.isHttp(url) && !HttpUtil.isHttps(url)) {
      if (url.startsWith("/")) {
        return URLUtil.completeUrl(currentUrl, url);
      } else {
        url =
            UrlBuilder.of(currentUrl).setQuery(UrlQuery.of(url, CharsetUtil.CHARSET_UTF_8)).build();
      }
    }
    return url;
  }

  @SuppressWarnings("unchecked")
  private static Object convert(
      Field field,
      ConvertType convertType,
      List<String> selected,
      List<Request> requests,
      Response response) {
    if (convertType.isArray() || convertType.isCollection()) {
      List<Object> list = new ArrayList<>();
      if (Convertors.isConvertibleType(convertType.getComponentType())) {
        for (String content : selected) {
          List<String> formatted = format(content, field, response);
          for (String s : formatted) {
            list.add(Convertors.convert(convertType.getComponentType(), s, field, response));
          }
        }
      } else if (convertType.getComponentType().isAnnotationPresent(Extractor.class)) {
        for (String content : selected) {
          List<String> formatted = format(content, field, response);
          for (String s : formatted) {
            Result<?> result =
                extract(s, convertType.getComponentType(), convertType.getActualType(), response);
            if (result.getObj() != null) {
              list.add(result.getObj());
            }
            if (result.getRequests() != null) {
              requests.addAll(result.getRequests());
            }
          }
        }
      }
      if (convertType.isArray()) {
        return list.toArray();
      } else {
        Collection<Object> collection =
            ReflectUtil.newInstance(VALID_COLLECTION_MAP.get(convertType.getCollectionClass()));
        collection.addAll(list);
        return collection;
      }
    } else if (Convertors.isConvertibleType(convertType.getComponentType())
        && !selected.isEmpty()) {
      List<String> formatted = format(selected.get(0), field, response);
      return formatted.isEmpty()
          ? null
          : Convertors.convert(convertType.getComponentType(), formatted.get(0), field, response);
    } else if (convertType.getComponentType().isAnnotationPresent(Extractor.class)
        && !selected.isEmpty()) {
      Result<?> result =
          extract(
              selected.get(0),
              convertType.getComponentType(),
              convertType.getActualType(),
              response);
      if (result.getRequests() != null) {
        requests.addAll(result.getRequests());
      }
      return result.getObj();
    }
    return null;
  }

  private static List<String> format(String content, Field field, Response response) {
    List<Annotation> multiLineFormats = Formatters.getMultiLineFormatAnnotations(field);
    if (multiLineFormats.isEmpty()) {
      return ListUtil.toList(Formatters.format(content, field, response));
    }
    List<String> formatted = Formatters.multiLineFormat(content, field);
    if (formatted == null || formatted.isEmpty()) {
      return Collections.emptyList();
    }
    return formatted.stream()
        .map(s -> Formatters.format(s, field, response))
        .collect(Collectors.toList());
  }
}
