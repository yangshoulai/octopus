package com.octopus.core.processor.extractor.convertor;

import com.octopus.core.Response;
import com.octopus.core.processor.extractor.ConvertException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class StringConvertorHandler implements ConvertorHandler<String, StringConvertor> {

  @Override
  public String convert(String val, StringConvertor format, Response response)
      throws ConvertException {
    return String.format(format.format(), (val == null ? format.def() : val));
  }
}
