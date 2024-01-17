package com.octopus.core.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/4
 */
public class AnnotationUtil {

  static final AnnotationFilter ANNOTATION_FILTER_INCLUDE_OCTOPUS = annotation -> annotation.annotationType().getName().startsWith("com.octopus.core");

  @SuppressWarnings("unchecked")
  public static <A extends Annotation> A proxySelfMergedAnnotation(A annotation, Class<A> annotationType) {
    return (A) Proxy.newProxyInstance(annotation.annotationType().getClassLoader(), new Class[]{annotationType},
        new SelfMergedAnnotationInvocationHandler(annotation));
  }

  @SuppressWarnings("unchecked")
  public static <A extends Annotation> A proxyInheritMergedAnnotation(Annotation annotation, Class<A> annotationType) {
    return (A) Proxy.newProxyInstance(annotation.annotationType().getClassLoader(), new Class[]{annotationType},
        new InheritMergedAnnotationInvocationHandler(annotation, annotationType));
  }

  public static <A extends Annotation> A getDirectlyMergedAnnotation(AnnotatedElement element, Class<A> annotationType) {
    A annotation = element.getAnnotation(annotationType);
    return annotation == null ? null : proxySelfMergedAnnotation(annotation, annotationType);
  }

  public static <A extends Annotation> List<A> getDirectlyMergedAnnotations(AnnotatedElement element, Class<A> annotationType) {
    A[] annotations = element.getAnnotationsByType(annotationType);
    return Arrays.stream(annotations).map(a -> proxySelfMergedAnnotation(a, annotationType)).collect(Collectors.toList());
  }

  public static List<Annotation> getDirectlyMergedAnnotations(AnnotatedElement element, AnnotationFilter filter) {
    Annotation[] annotations = element.getAnnotations();
    List<Annotation> merged = new ArrayList<>();
    for (Annotation annotation : annotations) {
      if (filter.filter(annotation)) {
        merged.add(getDirectlyMergedAnnotation(element, annotation.annotationType()));
      }
    }
    return merged;
  }

  public static <A extends Annotation> List<A> getMergedAnnotations(AnnotatedElement e, Class<A> annotationType) {
    return getMergedAnnotations(e, annotationType, ANNOTATION_FILTER_INCLUDE_OCTOPUS);
  }

  static <A extends Annotation> List<A> getMergedAnnotations(AnnotatedElement element, Class<A> annotationType, AnnotationFilter filter) {
    List<A> results = new ArrayList<>();
    Annotation[] annotations = element.getAnnotations();
    for (Annotation annotation : annotations) {
      if (filter.filter(annotation)) {
        if(annotation.annotationType()== annotationType){
          results.add(proxySelfMergedAnnotation((A)annotation, annotationType));
        }else{
          A a = getMergedAnnotation(annotation, annotationType, filter, new HashSet<>());
          if (a != null) {
            results.add(a);
          }
        }
      }
    }
    return results;
  }


  public static <A extends Annotation> A getMergedAnnotation(AnnotatedElement element, Class<A> annotationType) {
    return getMergedAnnotation(element, annotationType, ANNOTATION_FILTER_INCLUDE_OCTOPUS);
  }

  static <A extends Annotation> A getMergedAnnotation(AnnotatedElement element, Class<A> annotationType, AnnotationFilter filter) {
    A a = getDirectlyMergedAnnotation(element, annotationType);
    if (a != null) {
      return a;
    }
    List<Annotation> annotations = getDirectlyMergedAnnotations(element, filter);
    for (Annotation annotation : annotations) {
      a = getMergedAnnotation(annotation, annotationType, filter, new HashSet<>());
      if (a != null) {
        return a;
      }
    }
    return null;
  }

  private static <A extends Annotation> A getMergedAnnotation(Annotation source, Class<A> annotationType, AnnotationFilter filter,
      Set<Class<? extends Annotation>> visited) {
    if (!visited.contains(source.annotationType())) {
      visited.add(source.annotationType());
      if (source.annotationType().isAnnotationPresent(annotationType)) {
        return proxyInheritMergedAnnotation(source, annotationType);
      } else {
        List<Annotation> annotations = getInheritMergedAnnotations(source, filter);
        for (Annotation annotation : annotations) {
          A a = getMergedAnnotation(annotation, annotationType, filter, visited);
          if (a != null) {
            return a;
          }
        }
      }
    }
    return null;
  }

  private static List<Annotation> getInheritMergedAnnotations(Annotation source, AnnotationFilter filter) {
    Annotation[] annotations = source.annotationType().getAnnotations();
    List<Annotation> results = new ArrayList<>();
    for (Annotation annotation : annotations) {
      if (filter.filter(annotation)) {
        Annotation a = proxyInheritMergedAnnotation(source, annotation.annotationType());
        results.add(a);
      }
    }
    return results;
  }

  interface AnnotationFilter {

    boolean filter(Annotation annotation);
  }

  /**
   * @author shoulai.yang@gmail.com
   * @date 2023/4/4
   */
  public static class InheritMergedAnnotationInvocationHandler implements InvocationHandler {

    private final Annotation annotation;

    private final Class<? extends Annotation> annotationType;

    private final Annotation target;

    public InheritMergedAnnotationInvocationHandler(
        Annotation annotation, Class<? extends Annotation> annotationType) {
      this.annotation = annotation;
      this.annotationType = annotationType;
      this.target = getDirectlyMergedAnnotation(annotation.annotationType(), annotationType);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Method[] methods =
          ReflectUtil.getMethods(
              annotation.annotationType(),
              m -> {
                AliasFor aliasFor = getDirectlyMergedAnnotation(m, AliasFor.class);
                return aliasFor != null
                    && aliasFor.annotation() == annotationType
                    && method.getName().equals(aliasFor.value());
              });
      if (methods.length == 0) {
        return method.invoke(target, args);
      }
      for (Method m : methods) {
        Object o;
        if (annotation instanceof Proxy) {
          o = Proxy.getInvocationHandler(annotation).invoke(annotation, m, args);
        } else {
          o = ReflectUtil.invoke(annotation, m.getName(), args);
        }
        if (ObjectUtil.isNotEmpty(o)) {
          return o;
        }
      }
      return null;
    }
  }

  /**
   * @author shoulai.yang@gmail.com
   * @date 2023/4/4
   */
  public static class SelfMergedAnnotationInvocationHandler implements InvocationHandler {

    private final Annotation annotation;

    public SelfMergedAnnotationInvocationHandler(Annotation annotation) {
      this.annotation = annotation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Object o = method.invoke(annotation, args);
      if (ObjectUtil.isEmpty(o)) {
        Method[] methods =
            ReflectUtil.getMethods(
                annotation.annotationType(),
                m -> {
                  if (method == m) {
                    return false;
                  }
                  AliasFor aliasFor = getDirectlyMergedAnnotation(m, AliasFor.class);
                  return aliasFor != null
                      && aliasFor.annotation() == Annotation.class
                      && method.getName().equals(aliasFor.value());
                });
        for (Method m : methods) {
          if (annotation instanceof Proxy) {
            o = Proxy.getInvocationHandler(annotation).invoke(annotation, m, args);
          } else {
            o = m.invoke(annotation, args);
          }
          if (ObjectUtil.isNotEmpty(args)) {
            return o;
          }
        }
      }
      return o;
    }
  }
}
