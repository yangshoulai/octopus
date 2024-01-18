package com.octopus.core.processor.annotation;

import com.octopus.core.processor.annotation.Selector.Type;
import com.octopus.core.utils.AliasFor;

import java.lang.annotation.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/6
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Selector(type = Type.Value)
public @interface Value {
    @AliasFor(annotation = Selector.class, field = "value")
    String value() default "";
}
