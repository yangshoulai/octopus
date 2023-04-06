package com.octopus.core.processor.extractor.type;

import com.octopus.core.utils.AliasFor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface BooleanType {

  @AliasFor("falseValues")
  String[] value() default {};

  @AliasFor("value")
  String[] falseValues() default {};
}
