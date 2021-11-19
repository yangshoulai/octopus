package com.octopus.core;

/**
 * 请求下载器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public interface OctopusDownloader {

  /**
   * 下载
   *
   * @param request 下载请求
   * @return 下载响应
   */
  OctopusResponse download(OctopusRequest request);
}
