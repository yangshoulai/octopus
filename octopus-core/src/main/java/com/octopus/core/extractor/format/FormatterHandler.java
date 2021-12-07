package com.octopus.core.extractor.format;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public interface FormatterHandler<F extends Annotation> {

  String format(String val, F format);
}
