package com.octopus.core.extractor.converter;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class StringConverter extends AbstractTypeConverter<String> {

  @Override
  public String parse(String val, Annotation[] annotations) {
    return val;
  }

  @Override
  public Class<?>[] supportClasses() {
    return new Class[] {CharSequence.class, String.class};
  }
}
