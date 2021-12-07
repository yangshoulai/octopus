package com.octopus.core.extractor.selector;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/7
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RegexSelector {
  String expression();

  boolean multi() default true;

  boolean filter() default true;

  boolean self() default false;

  boolean trim() default true;

  int[] groups() default {0};

  String format() default "%s";
}
