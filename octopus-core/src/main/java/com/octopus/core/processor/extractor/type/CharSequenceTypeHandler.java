package com.octopus.core.processor.extractor.type;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class CharSequenceTypeHandler implements TypeHandler<CharSequence> {

  @Override
  public CharSequence handle(String source, Annotation annotation) {

    return source;
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return null;
  }
}
