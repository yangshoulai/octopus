package com.octopus.core.processor;

import com.octopus.core.Octopus;
import com.octopus.core.Response;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public interface Processor {

  /**
   * 解析响应
   *
   * @param response 下载响应
   * @param octopus octopus 实例
   */
  void process(Response response, Octopus octopus);
}
