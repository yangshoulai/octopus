package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.StrUtil;
import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class CharacterTypeHandler implements TypeHandler<Character> {

  @Override
  public Character handle(String source, Annotation annotation) {

    return StrUtil.isBlank(source) ? null : source.charAt(0);
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return null;
  }
}
