package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class IntegerTypeHandler implements TypeHandler<Integer> {

  @Override
  public Integer handle(String source, Annotation annotation) {
    IntegerType integerType = (IntegerType) annotation;
    try {
      if (StrUtil.isBlank(source)) {
        return null;
      }
      return NumberUtil.parseInt(source);
    } catch (Throwable e) {
      if (integerType != null && !integerType.ignorable()) {
        throw new OctopusException("Can not parse [" + source + "] to integer", e);
      }
      return null;
    }
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return IntegerType.class;
  }
}
