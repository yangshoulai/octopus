package com.octopus.core.extractor.converter;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class BooleanConverter extends AbstractTypeConverter<Boolean> {

  @Override
  protected Boolean parse(String val, Annotation[] annotations) {
    return Boolean.parseBoolean(val);
  }

  @Override
  public Class<?>[] supportClasses() {
    return new Class[] {boolean.class, Boolean.class};
  }
}
