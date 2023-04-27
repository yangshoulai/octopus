package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/27
 */
public class BigDecimalTypeHandler implements TypeHandler<BigDecimal> {

  @Override
  public BigDecimal handle(String source, Annotation annotation) {
    BigDecimalType type = (BigDecimalType) annotation;
    if (StrUtil.isNotEmpty(source)) {
      try {
        return new BigDecimal(source);
      } catch (Exception e) {
        if (type != null && !type.ignorable()) {
          throw new OctopusException("Can not parse [" + source + "] to big decimal", e);
        }
      }
    }
    return null;
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return BigDecimalType.class;
  }
}
