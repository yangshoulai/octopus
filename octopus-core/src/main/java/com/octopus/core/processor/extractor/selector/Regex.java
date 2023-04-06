package com.octopus.core.processor.extractor.selector;

import com.octopus.core.processor.extractor.selector.Selector.Type;
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
@Selector(type = Type.Regex)
public @interface Regex {

  @AliasFor(annotation = Selector.class, field = "value")
  String value() default "";

  @AliasFor(annotation = Selector.class, field = "value")
  String expression() default "";

  @AliasFor(annotation = Selector.class, field = "groups")
  int[] groups() default {0};

  @AliasFor(annotation = Selector.class, field = "format")
  String format() default "%s";

  @AliasFor(annotation = Selector.class, field = "formatter")
  Formatter formatter() default @Formatter();
}
