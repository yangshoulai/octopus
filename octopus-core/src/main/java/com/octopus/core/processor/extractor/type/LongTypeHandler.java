package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class LongTypeHandler implements TypeHandler<Long> {

  @Override
  public Long handle(String source, Annotation annotation) {
    LongType longType = (LongType) annotation;
    try {
      if (StrUtil.isBlank(source)) {
        return null;
      }
      return NumberUtil.parseLong(source);
    } catch (Throwable e) {
      if (longType != null && !longType.ignorable()) {
        throw new OctopusException("Can not parse [" + source + "] to long", e);
      }
      return null;
    }
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return LongType.class;
  }
}
