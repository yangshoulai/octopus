package com.octopus.core.extractor.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Extractor {

  Link[] links() default {};

  Type type() default Type.HTML;

  /** 页面数据类型 */
  enum Type {
    /** 网页类型 */
    HTML
  }
}
