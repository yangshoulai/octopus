package com.octopus.core.downloader;

import com.octopus.core.downloader.proxy.ProxyProvider;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 下载配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public interface DownloadConfig {

  /**
   * 获取下载请求头
   *
   * @return 下载请求头
   */
  Map<String, String> getHeaders();

  /**
   * 获取连接超时时间（毫秒）
   *
   * @return 超时时间
   */
  int getConnectTimeout();

  /**
   * 获取下载超时时间（毫秒）
   *
   * @return 下载超时时间
   */
  int getSocketTimeout();

  /**
   * 获取下载代理
   *
   * @return 下载代理
   */
  ProxyProvider getProxyProvider();

  /**
   * 获取默认编码
   *
   * @return 编码
   */
  Charset getCharset();
}
