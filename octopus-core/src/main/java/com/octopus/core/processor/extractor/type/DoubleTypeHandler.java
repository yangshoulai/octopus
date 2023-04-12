package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class DoubleTypeHandler implements TypeHandler<Double> {

  @Override
  public Double handle(String source, Annotation annotation) {
    DoubleType doubleType = (DoubleType) annotation;
    try {
      if (StrUtil.isBlank(source)) {
        return null;
      }
      return NumberUtil.parseDouble(source);
    } catch (Throwable e) {
      if (doubleType != null && !doubleType.ignorable()) {
        throw new OctopusException("Can not parse [" + source + "] to double", e);
      }
      return null;
    }
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return DoubleType.class;
  }
}
