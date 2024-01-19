package com.octopus.core.processor.annotation;

import com.octopus.core.utils.AliasFor;

import java.lang.annotation.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/18
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Selector(type = Selector.Type.Env)
public @interface Env {

    @AliasFor(annotation = Selector.class, field = "value")
    String value() default "";

    @AliasFor(annotation = Selector.class, field = "value")
    String name() default "";
}
