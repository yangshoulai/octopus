package com.octopus.core.processor.handler;

import cn.hutool.core.util.StrUtil;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class DoubleHandler implements TypeHandler<Double> {

  @Override
  public Double handle(String val) {
    return StrUtil.isNotBlank(val) ? Double.parseDouble(val.trim()) : null;
  }
}
