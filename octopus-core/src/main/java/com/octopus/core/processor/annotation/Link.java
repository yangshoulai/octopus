package com.octopus.core.processor.annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public @interface Link {

  CssSelector cssSelector();

  String format() default "";

  String pattern() default "";

  int[] groups() default {};

}
