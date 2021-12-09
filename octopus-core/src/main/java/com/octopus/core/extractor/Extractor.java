package com.octopus.core.extractor;

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

  /**
   * 匹配的网页URL正则表达式
   *
   * @return 正则表达式
   */
  Matcher[] matcher() default {};
}
