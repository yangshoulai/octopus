package com.octopus.core.extractor;

import com.octopus.core.Request.RequestMethod;
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.extractor.selector.CssSelector;
import com.octopus.core.extractor.selector.JsonSelector;
import com.octopus.core.extractor.selector.RegexSelector;
import com.octopus.core.extractor.selector.XpathSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(Links.class)
public @interface Link {

  CssSelector[] cssSelectors() default {};

  XpathSelector[] xpathSelectors() default {};

  JsonSelector[] jsonSelectors() default {};

  RegexSelector[] regexSelectors() default {};

  RegexFormatter[] formats() default {};

  int priority() default 0;

  boolean repeatable() default true;

  RequestMethod method() default RequestMethod.GET;
}
