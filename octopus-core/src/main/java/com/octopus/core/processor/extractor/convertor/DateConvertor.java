package com.octopus.core.processor.extractor.convertor;

import cn.hutool.core.date.DatePattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DateConvertor {

  String pattern() default DatePattern.NORM_DATETIME_PATTERN;

  String def() default "";

  boolean ignorable() default true;
}
