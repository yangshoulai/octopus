package com.octopus.core.processor.handler;

import cn.hutool.core.util.StrUtil;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class LongHandler implements TypeHandler<Long> {

  @Override
  public Long handle(String val) {
    return StrUtil.isNotBlank(val) ? Long.valueOf(val.trim()) : null;
  }
}
