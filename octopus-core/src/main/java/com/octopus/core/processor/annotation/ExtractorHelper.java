package com.octopus.core.processor.annotation;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.processor.handler.DoubleHandler;
import com.octopus.core.processor.handler.FloatHandler;
import com.octopus.core.processor.handler.IntegerHandler;
import com.octopus.core.processor.handler.LongHandler;
import com.octopus.core.processor.handler.ShortHandler;
import com.octopus.core.processor.handler.StringHandler;
import com.octopus.core.processor.handler.TypeHandler;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class ExtractorHelper {

  private static final Map<Class<?>, TypeHandler<?>> TYPES = new HashMap<>();

  static {
    TYPES.put(int.class, new IntegerHandler());
    TYPES.put(Integer.class, new IntegerHandler());
    TYPES.put(long.class, new LongHandler());
    TYPES.put(Long.class, new LongHandler());
    TYPES.put(short.class, new ShortHandler());
    TYPES.put(Short.class, new ShortHandler());
    TYPES.put(double.class, new DoubleHandler());
    TYPES.put(Double.class, new DoubleHandler());
    TYPES.put(float.class, new FloatHandler());
    TYPES.put(Float.class, new FloatHandler());
    TYPES.put(CharSequence.class, new StringHandler());
    TYPES.put(String.class, new StringHandler());
  }

  public static <T> Pair<T, List<String>> extract(String content, Class<T> cls) {
    T obj = null;
    List<String> links = new ArrayList<>();
    if (isValidExtractorClass(cls)
        && StrUtil.isNotBlank(content)
        && cls.isAnnotationPresent(Extractor.class)) {

      // 获取数据信息
      obj = ReflectUtil.newInstance(cls);
      Field[] fields = cls.getDeclaredFields();
      for (Field field : fields) {
        if (isFieldHasSelectorAnnotation(field)) {
          if (field.isAnnotationPresent(CssSelector.class)) {
            extractCss(Jsoup.parse(content), field, obj, links);
          }
        }
      }

      // 获取连接
      Extractor extractor = cls.getAnnotation(Extractor.class);
      Link[] linkArray = extractor.links();
      if (linkArray != null) {
        for (Link link : linkArray) {
          List<String> selected = new ArrayList<>();
          if (link.cssSelector() != null) {
            selected.addAll(extractLinkCss(content, link.cssSelector()));
          }
          // 连接变换
          if (!selected.isEmpty()) {
            String format = link.format();
            String pattern = link.pattern();
            int[] groups =
                link.groups() == null || link.groups().length <= 0 ? new int[] {0} : link.groups();
            selected =
                selected.stream()
                    .map(String::trim)
                    .map(
                        l -> {
                          if (StrUtil.isNotBlank(format)) {
                            List<String> args = new ArrayList<>();
                            if (StrUtil.isNotBlank(pattern)) {
                              for (int group : groups) {
                                args.add(ReUtil.get(pattern, l, group));
                              }
                            } else {
                              args.add(l);
                            }
                            return String.format(format, args.toArray());
                          }
                          return l;
                        })
                    .collect(Collectors.toList());
            links.addAll(selected);
          }
        }
      }
    }
    return Pair.of(obj, links);
  }

  private static List<String> extractLinkCss(String content, CssSelector cssSelector) {
    List<String> links = new ArrayList<>();
    String selector = cssSelector.value();
    String attr = cssSelector.attribute();
    boolean useText = cssSelector.useText();
    Elements elements = Jsoup.parse(content).select(selector);
    elements.forEach(
        e -> {
          if (!StrUtil.isBlank(attr) && !StrUtil.isBlank(e.attr(attr))) {
            links.add(e.attr(attr));
          } else if (useText && !StrUtil.isBlank(e.text())) {
            links.add(e.text());
          }
        });
    return links;
  }

  private static void extractCss(Element element, Field field, Object obj, List<String> links) {
    CssSelector selector = field.getAnnotation(CssSelector.class);
    String val = selector.value();
    String attr = selector.attribute();
    boolean useText = selector.useText();
    Elements elements = element.select(val);
    if (elements.isEmpty()) {
      return;
    }
    Class<?> clz = TypeUtil.getClass(field);
    if (Collection.class.isAssignableFrom(clz) || clz.isArray()) {
      Class<?> argClz = clz.isArray() ? clz.getComponentType() : getCollectionArgClass(field, clz);
      List<?> list = null;
      if (TYPES.containsKey(argClz)) {
        if (!StrUtil.isNotBlank(attr) || useText) {
          list =
              elements.stream()
                  .map(
                      e -> {
                        if (StrUtil.isNotBlank(attr)) {
                          return e.attr(attr);
                        }
                        return e.text();
                      })
                  .map(r -> TYPES.get(argClz).handle(r))
                  .collect(Collectors.toList());
        }
      } else {
        list =
            elements.stream()
                .map(Element::html)
                .map(r -> extract(r, argClz))
                .peek(p -> links.addAll(p.getValue()))
                .map(Pair::getKey)
                .collect(Collectors.toList());
      }
      setCollectionField(obj, field, clz, list);
    } else if (TYPES.containsKey(clz)) {
      String v = null;
      if (StrUtil.isNotBlank(attr)) {
        v = elements.get(0).attr(attr);
      } else if (useText) {
        v = elements.get(0).text();
      }
      ReflectUtil.setFieldValue(obj, field, v);
    } else if (clz.isAnnotationPresent(Extractor.class)) {
      Pair<?, List<String>> subPair = extract(elements.first().html(), clz);
      ReflectUtil.setFieldValue(obj, field, subPair.getKey());
      links.addAll(subPair.getValue());
    }
  }

  private static void setCollectionField(Object obj, Field field, Class<?> fieldCls, List<?> list) {
    if (List.class.equals(fieldCls) || Collection.class.equals(fieldCls)) {
      ReflectUtil.setFieldValue(obj, field, list);
    } else if (Set.class.equals(fieldCls)) {
      ReflectUtil.setFieldValue(obj, field, new HashSet<>(list));
    } else {
      ReflectUtil.setFieldValue(obj, field, list.toArray());
    }
  }

  private static Class<?> getCollectionArgClass(Field field, Class<?> fieldCollectionClass) {
    Type actualType = TypeUtil.getActualType(fieldCollectionClass, field);
    if (actualType != null) {
      Type[] types = TypeUtil.toParameterizedType(actualType).getActualTypeArguments();
      if (types != null && types.length > 0) {
        return TypeUtil.getClass(types[0]);
      }
    }
    return null;
  }

  public static boolean isValidExtractorClass(Class<?> cls) {
    if (cls.isAnnotationPresent(Extractor.class)) {
      Field[] fields = cls.getDeclaredFields();
      for (Field field : fields) {
        if (isFieldHasSelectorAnnotation(field)) {
          Class<?> fieldClass = TypeUtil.getClass(field);
          if (Collection.class.isAssignableFrom(fieldClass)) {
            // 集合类型 判断集合元素真实类型
            if (Collection.class.equals(fieldClass)
                || List.class.equals(fieldClass)
                || Set.class.equals(fieldClass)) {
              Class<?> argClass = getCollectionArgClass(field, fieldClass);
              if (argClass == null || isNotValidClass(argClass)) {
                return false;
              }
            } else {
              return false;
            }
          } else if (fieldClass.isArray()) {
            // 数据类型 判断数据元素真实类型
            Class<?> argClass = fieldClass.getComponentType();
            if (isNotValidClass(argClass)) {
              return false;
            }
          } else if (isNotValidClass(fieldClass)) {
            // 常见类型 或者 被 @Extractor注解标记的自定义对象
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }

  private static boolean isNotValidClass(Class<?> cls) {
    // 常见类型
    return !TYPES.containsKey(cls)
        && (!cls.isAnnotationPresent(Extractor.class) || !isValidExtractorClass(cls));
  }

  private static boolean isFieldHasSelectorAnnotation(Field field) {
    return field.isAnnotationPresent(CssSelector.class);
  }

  public static void main(String[] args) throws IOException {
    String content =
        Jsoup.connect(
                "https://wallhaven.cc/search?categories=110&purity=010&ratios=16x9%2C16x10&sorting=hot&order=desc&page=1")
            .get()
            .html();
    Pair<Result, List<String>> pair = extract(content, Result.class);
    System.out.println(pair);
  }

  @Extractor(
      links = {
        @Link(cssSelector = @CssSelector(value = "#thumbs ul li img", attribute = "data-src")),
        @Link(cssSelector = @CssSelector(value = "ul.pagination li a.next", attribute = "href"))
      })
  public static class Result {

    @CssSelector(value = "#thumbs ul li")
    private List<Item> items;

    public List<Item> getItems() {
      return items;
    }

    public void setItems(List<Item> items) {
      this.items = items;
    }
  }

  @Extractor
  public static class Item {

    @CssSelector(value = "img", attribute = "data-src")
    private String src;

    @CssSelector(value = "a.preview", attribute = "href")
    private String previewSrc;

    public String getSrc() {
      return src;
    }

    public void setSrc(String src) {
      this.src = src;
    }

    public String getPreviewSrc() {
      return previewSrc;
    }

    public void setPreviewSrc(String previewSrc) {
      this.previewSrc = previewSrc;
    }
  }
}
