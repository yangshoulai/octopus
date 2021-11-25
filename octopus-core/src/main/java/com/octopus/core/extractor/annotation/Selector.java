package com.octopus.core.extractor.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Selector {

  Type type() default Type.CSS;

  String expression();

  String attr() default "";

  boolean multi() default true;

  boolean filter() default true;

  boolean self() default true;

  /** 选择器类型 */
  enum Type {
    /** CSS选择器 */
    CSS,

    /** XPATH 选择器 */
    XPATH
  }
}
