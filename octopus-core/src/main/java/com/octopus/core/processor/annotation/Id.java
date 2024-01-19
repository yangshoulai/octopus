package com.octopus.core.processor.annotation;

import java.lang.annotation.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/18
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Selector(type = Selector.Type.Id)
public @interface Id {
}
