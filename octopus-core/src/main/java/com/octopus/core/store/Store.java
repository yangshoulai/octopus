package com.octopus.core.store;

import com.octopus.core.Request;

/**
 * 请求存储器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public interface Store {

  /**
   * 获取一个请求
   *
   * @return 下载请求
   */
  Request get();

  /**
   * 存入一个请求
   *
   * @param request 下载请求
   */
  boolean put(Request request);
}
