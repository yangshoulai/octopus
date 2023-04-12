package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class FloatTypeHandler implements TypeHandler<Float> {

  @Override
  public Float handle(String source, Annotation annotation) {

    FloatType floatType = (FloatType) annotation;
    try {
      if (StrUtil.isBlank(source)) {
        return null;
      }
      return NumberUtil.parseFloat(source);
    } catch (Throwable e) {
      if (floatType != null && !floatType.ignorable()) {
        throw new OctopusException("Can not parse [" + source + "] to float", e);
      }
      return null;
    }
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return FloatType.class;
  }
}
