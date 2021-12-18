package com.octopus.core.extractor.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/9
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Prop {

  String name() default "";

  String value() default "";

  String field() default "";
}
