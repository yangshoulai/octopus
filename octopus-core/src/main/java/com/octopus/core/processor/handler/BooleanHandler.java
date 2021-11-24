package com.octopus.core.processor.handler;

import cn.hutool.core.util.StrUtil;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class BooleanHandler implements TypeHandler<Boolean> {

  @Override
  public Boolean handle(String val) {
    return StrUtil.isNotBlank(val) ? Boolean.parseBoolean(val.trim()) : null;
  }
}
