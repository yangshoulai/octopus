package com.octopus.core;

/**
 * 请求存储器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public interface OctopusStore {

  /**
   * 获取一个请求
   *
   * @return 下载请求
   */
  OctopusRequest get();

  /**
   * 存入一个请求
   *
   * @param request 下载请求
   */
  void put(OctopusRequest request);
}
