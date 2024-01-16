package com.octopus.core.processor.annotation;

import java.lang.annotation.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldExt {

    boolean ignoreError() default true;

    String[] booleanFalseValues() default {"", "0", "非", "否", "off", "no", "f", "false"};

    String dateFormatPattern() default "yyyy-MM-dd HH:mm:ss";

    String dateFormatTimeZone() default "";
}
