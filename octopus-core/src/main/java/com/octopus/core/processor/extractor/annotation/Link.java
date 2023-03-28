package com.octopus.core.processor.extractor.annotation;

import com.octopus.core.Request.RequestMethod;
import com.octopus.core.processor.extractor.selector.Selector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
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
@Repeatable(Links.class)
public @interface Link {

  String[] url() default {};

  Selector[] selectors() default {};


  int priority() default 0;

  boolean repeatable() default true;

  boolean inherit() default false;

  RequestMethod method() default RequestMethod.GET;

  Prop[] params() default {};

  Prop[] headers() default {};

  Prop[] attrs() default {};
}
