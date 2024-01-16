package com.octopus.core.processor.annotation;

import com.octopus.core.Request.RequestMethod;
import com.octopus.core.processor.annotation.Selector.Type;
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
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Link {

  String url() default "";

  Selector selector() default @Selector(type = Type.None);

  int priority() default 0;

  boolean repeatable() default true;

  boolean inherit() default false;

  RequestMethod method() default RequestMethod.GET;

  Prop[] params() default {};

  Prop[] headers() default {};

  Prop[] attrs() default {};
}
