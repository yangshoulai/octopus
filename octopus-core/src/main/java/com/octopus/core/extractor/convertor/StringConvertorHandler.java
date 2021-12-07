package com.octopus.core.extractor.convertor;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class StringConvertorHandler implements ConvertorHandler<String, StringConvertor> {

  @Override
  public String convert(String val, StringConvertor format) {
    if (format != null) {
      return String.format(format.format(), (val == null ? format.def() : val));
    }
    return val;
  }

  @Override
  public Class<?>[] getSupportClasses() {
    return new Class[] {CharSequence.class, String.class};
  }
}
