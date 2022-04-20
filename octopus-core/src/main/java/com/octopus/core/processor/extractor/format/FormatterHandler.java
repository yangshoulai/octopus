package com.octopus.core.processor.extractor.format;

import com.octopus.core.Response;
import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public interface FormatterHandler<F extends Annotation> {

  String format(String val, F format, Response response);
}
