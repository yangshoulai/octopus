package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.StrUtil;
import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class BooleanTypeHandler implements TypeHandler<Boolean> {

  @Override
  public Boolean handle(String source, Annotation annotation) {
    BooleanType booleanType = (BooleanType) annotation;
    if (StrUtil.isBlank(source)) {
      return Boolean.FALSE;
    }
    if (booleanType != null
        && booleanType.falseValues() != null
        && booleanType.falseValues().length > 0) {
      if (Arrays.asList(booleanType.falseValues()).contains(source)) {
        return Boolean.FALSE;
      }
    }
    return Boolean.TRUE;
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return BooleanType.class;
  }
}
