package com.octopus.core.extractor.annotation;

import com.octopus.core.processor.matcher.Matchers;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/9
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Matcher {

  Type type() default Type.ALL;

  String regex() default "^[\\s\\S]*$";

  String header() default "";

  enum Type {
    /** 匹配所有 */
    ALL {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.ALL;
      }
    },

    /** URL正则匹配 */
    URL_REGEX {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.urlRegex(matcher.regex());
      }
    },

    /** 响应头正则匹配 */
    HEADER_REGEX {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.headerRegex(matcher.header(), matcher.regex());
      }
    },
    /** 响应内容正则匹配 */
    CONTENT_TYPE_REGEX {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.contentType(matcher.regex());
      }
    },

    HTML {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.HTML;
      }
    },

    JSON {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.JSON;
      }
    },

    IMAGE {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.IMAGE;
      }
    },

    VIDEO {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.VIDEO;
      }
    },

    AUDIO {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.AUDIO;
      }
    },

    OCTET_STREAM {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.OCTET_STREAM;
      }
    },

    MEDIA {
      @Override
      public com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher) {
        return Matchers.or(Matchers.VIDEO, Matchers.AUDIO, Matchers.IMAGE, Matchers.OCTET_STREAM);
      }
    };

    public abstract com.octopus.core.processor.matcher.Matcher resolve(Matcher matcher);
  }
}
