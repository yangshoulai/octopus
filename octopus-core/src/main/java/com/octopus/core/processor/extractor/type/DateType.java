package com.octopus.core.processor.extractor.type;

import com.octopus.core.utils.AliasFor;
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
public @interface DateType {

  @AliasFor("pattern")
  String value() default "";

  @AliasFor("value")
  String pattern() default "";


  String timeZone() default "";

  // 是否忽略异常
  boolean ignorable() default true;
}
