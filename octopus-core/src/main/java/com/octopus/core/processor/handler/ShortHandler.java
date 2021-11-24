package com.octopus.core.processor.handler;

import cn.hutool.core.util.StrUtil;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class ShortHandler implements TypeHandler<Short> {

  @Override
  public Short handle(String val) {
    return StrUtil.isNotBlank(val) ? Short.parseShort(val.trim()) : null;
  }
}
