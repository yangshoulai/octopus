package com.octopus.core;

import com.octopus.core.exception.DownloadException;
import java.util.List;

/**
 * 爬虫拦截器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public interface OctopusListener {

  /**
   * 请求进入队列之前
   *
   * @param request 下载请求
   */
  default void beforeStore(Request request) {}

  /**
   * 请求出列之后，下载之前
   *
   * @param request 下载请求
   */
  default void beforeDownload(Request request) {}

  /**
   * 下载异常
   *
   * @param request 下载请求
   * @param e 下载异常
   */
  default void onDownloadError(Request request, DownloadException e) {
  }

  /**
   * 请求下载之后，处理之前
   *
   * @param response 下载响应
   */
  default void beforeProcess(Response response) {}

  /**
   * 请求处理之后
   *
   * @param response 下载响应
   * @param newRequests 新的下载请求
   */
  default void afterProcess(Response response, List<Request> newRequests) {}
}
