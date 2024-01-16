package com.octopus.core.processor.annotation;

import com.octopus.core.processor.annotation.Selector.Type;
import com.octopus.core.utils.AliasFor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/6
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Selector(type = Type.Param)
public @interface Param {

  @AliasFor(annotation = Selector.class, field = "value")
  String value() default "";

  @AliasFor(annotation = Selector.class, field = "value")
  String name() default "";

  @AliasFor(annotation = Selector.class, field = "formatter")
  Formatter formatter() default @Formatter();
}
