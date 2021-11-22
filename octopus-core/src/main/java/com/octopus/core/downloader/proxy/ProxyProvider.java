package com.octopus.core.downloader.proxy;

import com.octopus.core.Request;
import java.net.Proxy;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public interface ProxyProvider {

  /**
   * 提供一个下载代理
   *
   * @param request 下载请求
   * @return 下载代理
   */
  Proxy provide(Request request);
}
