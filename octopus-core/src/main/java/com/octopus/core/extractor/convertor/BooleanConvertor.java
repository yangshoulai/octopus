package com.octopus.core.extractor.convertor;

import cn.hutool.core.util.StrUtil;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class BooleanConvertor implements Convertor<Boolean, BooleanVal> {

  @Override
  public Boolean convert(String val, BooleanVal format) {
    if (StrUtil.isBlank(val)) {
      return format != null && format.def();
    }
    return !"0".equals(val);
  }

  @Override
  public Class<?>[] getSupportClasses() {
    return new Class[] {boolean.class, Boolean.class};
  }
}
