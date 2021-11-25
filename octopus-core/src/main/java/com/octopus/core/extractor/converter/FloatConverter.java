package com.octopus.core.extractor.converter;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class FloatConverter extends AbstractTypeConverter<Float> {

  @Override
  protected Float parse(String val, Annotation[] annotations) {
    return Float.parseFloat(val);
  }

  @Override
  public Class<?>[] supportClasses() {
    return new Class[] {float.class, Float.class};
  }
}
