package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.NumberUtil;
import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class FloatTypeHandler implements TypeHandler<Float> {

  @Override
  public Float handle(String source, Annotation annotation) {

    return NumberUtil.parseFloat(source);
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return null;
  }
}
