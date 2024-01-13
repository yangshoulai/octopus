package com.octopus.core.processor.extractor.annotation;

import com.octopus.core.processor.extractor.annotation.Selector.Type;
import com.octopus.core.utils.AliasFor;

import java.lang.annotation.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/6
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Selector(type = Type.Header)
public @interface Header {

    @AliasFor(annotation = Selector.class, field = "value")
    String value() default "";

    @AliasFor(annotation = Selector.class, field = "value")
    String name() default "";

    @AliasFor(annotation = Selector.class, field = "formatter")
    Formatter formatter() default @Formatter();
}
