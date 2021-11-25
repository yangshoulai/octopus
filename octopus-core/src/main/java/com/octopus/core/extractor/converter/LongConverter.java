package com.octopus.core.extractor.converter;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class LongConverter extends AbstractTypeConverter<Long> {

  @Override
  protected Long parse(String val, Annotation[] annotations) {
    return Long.valueOf(val);
  }

  @Override
  public Class<?>[] supportClasses() {
    return new Class[] {long.class, Long.class};
  }
}
