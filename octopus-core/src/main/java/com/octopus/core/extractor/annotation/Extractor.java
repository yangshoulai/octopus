package com.octopus.core.extractor.annotation;

import java.lang.annotation.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Extractor {

  Link[] links() default {};
}
