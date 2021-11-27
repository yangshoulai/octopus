package com.octopus.core.extractor.annotation;

import java.lang.annotation.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/28
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Links {}
