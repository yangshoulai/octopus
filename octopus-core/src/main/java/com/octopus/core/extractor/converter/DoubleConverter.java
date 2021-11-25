package com.octopus.core.extractor.converter;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class DoubleConverter extends AbstractTypeConverter<Double> {

  @Override
  protected Double parse(String val, Annotation[] annotations) {
    return Double.parseDouble(val);
  }

  @Override
  public Class<?>[] supportClasses() {
    return new Class[] {double.class, Double.class};
  }
}
