package com.octopus.core.processor.extractor;

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

    String[] booleanFalseValues() default {};

    String dateFormatPattern() default "";

    String dateFormatTimeZone() default "";
}
