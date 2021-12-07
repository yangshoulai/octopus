package com.octopus.core.extractor.format;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
public interface MultiLineFormatterHandler<F extends Annotation> {

  List<String> format(String val, F format);
}
