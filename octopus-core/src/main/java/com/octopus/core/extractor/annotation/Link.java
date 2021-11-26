package com.octopus.core.extractor.annotation;

import com.octopus.core.Request.RequestMethod;
import com.octopus.core.extractor.format.RegexFormat;
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
public @interface Link {

  Selector selector();

  RegexFormat[] formats() default {};

  int priority() default 0;

  boolean repeatable() default true;

  RequestMethod method() default RequestMethod.GET;
}
