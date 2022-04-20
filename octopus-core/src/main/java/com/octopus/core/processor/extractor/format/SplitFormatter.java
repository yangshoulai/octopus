package com.octopus.core.processor.extractor.format;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SplitFormatter {

  String regex() default "；|;|，|,|#| |、|/|\\\\|\\|";

  int limit() default 0;

  boolean trim() default true;

  boolean filter() default true;

}
