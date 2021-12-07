package com.octopus.core.extractor;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.extractor.selector.CacheableSelectorHandler;
import com.octopus.core.extractor.selector.CssSelectorHandler;
import com.octopus.core.extractor.selector.JsonSelectorHandler;
import com.octopus.core.extractor.selector.RegexSelectorHandler;
import com.octopus.core.extractor.selector.SelectorHandler;
import com.octopus.core.extractor.selector.XpathSelectorHandler;
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
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Slf4j
public class Selectors {

  private static final Map<Class<? extends Annotation>, SelectorHandler<? extends Annotation>>
      SELECTORS = new HashMap<>();

  static {
    registerSelector(new CssSelectorHandler());
    registerSelector(new XpathSelectorHandler());
    registerSelector(new JsonSelectorHandler());
    registerSelector(new RegexSelectorHandler());
  }

  @SuppressWarnings("unchecked")
  public static void registerSelector(@NonNull SelectorHandler<? extends Annotation> selector) {
    Type type = null;
    Type[] types = TypeUtil.getTypeArguments(selector.getClass());
    if (types != null && types.length > 0) {
      if (selector instanceof CacheableSelectorHandler) {
        if (types.length > 1) {
          type = types[1];
        }
      } else {
        type = types[0];
      }
    }
    Class<? extends Annotation> cls = (Class<? extends Annotation>) TypeUtil.getClass(type);
    if (cls != null) {
      if (SELECTORS.containsKey(cls)) {
        throw new OctopusException("Selector handler for annotation " + cls + " already exists");
      } else {
        SELECTORS.put(cls, selector);
      }
    } else {
      throw new OctopusException(
          "Not a valid selector handler, selector handler must directly extends CacheableSelector or implements SelectorHandler");
    }
  }

  static List<String> select(String content, Annotation selector) {
    if (!SELECTORS.containsKey(selector.annotationType())) {
      throw new OctopusException("No selector found for type " + selector.annotationType());
    }
    SelectorHandler<? extends Annotation> selectorHandler =
        SELECTORS.get(selector.annotationType());
    Method method =
        ReflectUtil.getMethod(selectorHandler.getClass(), "select", String.class, Annotation.class);
    return ReflectUtil.invoke(selectorHandler, method, content, selector);
  }

  static List<String> select(String content, Field field) {
    List<Annotation> annotations = getSelectorAnnotations(field);
    for (Annotation annotation : annotations) {
      SelectorHandler<? extends Annotation> selectorHandler =
          SELECTORS.get(annotation.annotationType());
      try {
        Method method =
            ReflectUtil.getMethod(
                selectorHandler.getClass(), "select", String.class, Annotation.class);
        List<String> results = ReflectUtil.invoke(selectorHandler, method, content, annotation);
        if (results != null && !results.isEmpty()) {
          return results;
        }
      } catch (Throwable e) {
        log.error("", e);
      }
    }
    return Collections.emptyList();
  }

  static List<Annotation> getSelectorAnnotations(Field field) {
    List<Annotation> selectorAnnotations = new ArrayList<>();
    Annotation[] annotations = field.getAnnotations();
    for (Annotation annotation : annotations) {
      if (SELECTORS.containsKey(annotation.annotationType())) {
        selectorAnnotations.add(annotation);
      }
    }
    return selectorAnnotations;
  }

  static boolean hasSelectorAnnotation(Field field) {
    return !getSelectorAnnotations(field).isEmpty();
  }
}
