package com.octopus.core.processor.handler;

import cn.hutool.core.util.StrUtil;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class FloatHandler implements TypeHandler<Float> {

  @Override
  public Float handle(String val) {
    return StrUtil.isNotBlank(val) ? Float.parseFloat(val.trim()) : null;
  }
}
