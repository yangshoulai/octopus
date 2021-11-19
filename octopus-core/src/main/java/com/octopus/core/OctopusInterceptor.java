package com.octopus.core;

import java.util.List;

/**
 * 爬虫拦截器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public interface OctopusInterceptor {

  /**
   * 请求进入队列之前
   *
   * @param request 下载请求
   */
  default void preStore(OctopusRequest request) {}

  /**
   * 请求出列之后，下载之前
   *
   * @param request 下载请求
   */
  default void preDownload(OctopusRequest request) {}

  /**
   * 请求下载之后，处理之前
   *
   * @param response 下载响应
   */
  default void preProcess(OctopusResponse response) {}

  /**
   * 请求处理之后
   *
   * @param response 下载响应
   * @param newRequests 新的下载请求
   */
  default void postProcess(OctopusResponse response, List<OctopusRequest> newRequests) {}
}
