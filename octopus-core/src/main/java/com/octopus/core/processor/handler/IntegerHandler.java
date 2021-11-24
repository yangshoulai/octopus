package com.octopus.core.processor.handler;

import cn.hutool.core.util.StrUtil;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class IntegerHandler implements TypeHandler<Integer> {

  @Override
  public Integer handle(String val) {
    return StrUtil.isNotBlank(val) ? Integer.valueOf(val.trim()) : null;
  }
}
