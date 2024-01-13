package com.octopus.core.processor.extractor.annotation;

import com.octopus.core.processor.extractor.annotation.Selector.Type;
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
@Selector(type = Type.Css)
public @interface Css {

  @AliasFor(annotation = Selector.class, field = "value")
  String value() default "";

  @AliasFor(annotation = Selector.class, field = "value")
  String expression() default "";

  @AliasFor(annotation = Selector.class, field = "attr")
  String attr() default "";

  @AliasFor(annotation = Selector.class, field = "self")
  boolean self() default false;

  @AliasFor(annotation = Selector.class, field = "formatter")
  Formatter formatter() default @Formatter();
}
