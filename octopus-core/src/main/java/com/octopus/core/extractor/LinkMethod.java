package com.octopus.core.extractor;

import java.lang.annotation.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/28
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LinkMethod {}
