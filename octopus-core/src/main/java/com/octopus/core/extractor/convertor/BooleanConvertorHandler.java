package com.octopus.core.extractor.convertor;

import cn.hutool.core.util.StrUtil;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class BooleanConvertorHandler implements ConvertorHandler<Boolean, BooleanConvertor> {

  @Override
  public Boolean convert(String val, BooleanConvertor format) {
    if (StrUtil.isBlank(val)) {
      return format != null && format.def();
    }
    return !"0".equals(val) && !"false".equalsIgnoreCase(val);
  }

  @Override
  public Class<?>[] getSupportClasses() {
    return new Class[] {boolean.class, Boolean.class};
  }
}
