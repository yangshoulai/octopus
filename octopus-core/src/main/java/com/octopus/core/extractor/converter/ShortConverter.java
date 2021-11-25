package com.octopus.core.extractor.converter;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class ShortConverter extends AbstractTypeConverter<Short> {

  @Override
  protected Short parse(String val, Annotation[] annotations) {
    return Short.parseShort(val);
  }

  @Override
  public Class<?>[] supportClasses() {
    return new Class[] {short.class, Short.class};
  }
}
