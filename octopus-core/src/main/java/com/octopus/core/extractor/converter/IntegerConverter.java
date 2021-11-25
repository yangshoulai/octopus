package com.octopus.core.extractor.converter;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class IntegerConverter extends AbstractTypeConverter<Integer> {

  @Override
  protected Integer parse(String val, Annotation[] annotations) {
    return Integer.valueOf(val);
  }

  @Override
  public Class<?>[] supportClasses() {
    return new Class[] {int.class, Integer.class};
  }
}
