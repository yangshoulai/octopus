package com.octopus.core.processor.extractor.type;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class BooleanTypeHandler implements TypeHandler<Boolean> {

  private static final List<String> FALSE_VALUES =
      ListUtil.toList("", "0", "非", "否", "off", "no", "f","false");

  @Override
  public Boolean handle(String source, Annotation annotation) {
    BooleanType booleanType = (BooleanType) annotation;
    if (StrUtil.isBlank(source)) {
      return Boolean.FALSE;
    }
    if (booleanType != null && booleanType.falseValues().length > 0) {
      return !Arrays.asList(booleanType.falseValues()).contains(source.toLowerCase());
    }
    return !FALSE_VALUES.contains(source);
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return BooleanType.class;
  }
}
