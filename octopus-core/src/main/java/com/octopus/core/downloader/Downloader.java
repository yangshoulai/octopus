package com.octopus.core.downloader;

import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.exception.DownloadException;

/**
 * 请求下载器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public interface Downloader {

  /**
   * 下载
   *
   * @param request 下载请求
   * @param config 下载配置
   * @return 下载响应
   * @throws DownloadException 下载异常
   */
  Response download(Request request, DownloadConfig config) throws DownloadException;
}
