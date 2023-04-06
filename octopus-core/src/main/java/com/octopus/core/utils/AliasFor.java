package com.octopus.core.utils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/4
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasFor {

  Class<? extends Annotation> annotation() default Annotation.class;

  @AliasFor("value")
  String field() default "";

  @AliasFor("field")
  String value() default "";
}
