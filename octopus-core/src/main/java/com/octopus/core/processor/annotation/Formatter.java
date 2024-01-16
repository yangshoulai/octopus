package com.octopus.core.processor.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Formatter {

  /**
   * @return 是否去除首尾空白
   */
  boolean trim() default true;

  /**
   * @return 是否过滤空白行
   */
  boolean filter() default true;

  /**
   * @return 是否分割
   */
  boolean split() default false;

  /**
   * @return 分隔符
   */
  String separator() default "；|;|，|,|#| |、|/|\\\\|\\|";

  /**
   * @return 正则提取
   */
  String regex() default ""; // ^[\w\W]*$

  /**
   * @return 正则提取格式化
   */
  String format() default "%s";

  /**
   * @return 正则提取的分组
   */
  int[] groups() default 0;
}
