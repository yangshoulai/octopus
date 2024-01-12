package com.octopus.core.downloader;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.Header;
import com.octopus.core.downloader.proxy.DefaultProxyProvider;
import com.octopus.core.downloader.proxy.ProxyProvider;
import lombok.NonNull;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 下载配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class DownloadConfig {

  public static final String DEFAULT_UA =
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36";

  public static final String DEFAULT_ACCEPT = "*/*";

  public static final Map<String, String> DEFAULT_HEADERS =
      MapUtil.unmodifiable(
          MapUtil.builder(Header.ACCEPT.getValue(), DEFAULT_ACCEPT)
              .put(Header.USER_AGENT.getValue(), DEFAULT_UA)
              .build());

  public static final int DEFAULT_CONNECT_TIMEOUT = 60000;

  public static final int DEFAULT_SOCKET_TIME = 60000;

  public static final ProxyProvider DEFAULT_PROXY_PROVIDER = new DefaultProxyProvider();

  public static final Charset DEFAULT_CHARSET = CharsetUtil.CHARSET_UTF_8;

  private Map<String, String> headers = new HashMap<>(DEFAULT_HEADERS);

  private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

  private int socketTimeout = DEFAULT_SOCKET_TIME;

  private ProxyProvider proxyProvider = DEFAULT_PROXY_PROVIDER;

  private Charset charset = DEFAULT_CHARSET;

  public DownloadConfig() {}

  public DownloadConfig(@NonNull ProxyProvider proxyProvider) {
    this.proxyProvider = proxyProvider;
  }

  public DownloadConfig(@NonNull Map<String, String> headers) {
    this.headers.putAll(headers);
  }

  /**
   * 设置请求头
   *
   * @param headers 请求头
   * @return 当前下载配置
   */
  public DownloadConfig setHeaders(@NonNull Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  /**
   * 获取下载请求头
   *
   * @return 下载请求头
   */
  public Map<String, String> getHeaders() {
    return this.headers;
  }

  /**
   * 设置请求头
   *
   * @param headers 请求头
   * @return 当前下载配置
   */
  public DownloadConfig addHeaders(@NonNull Map<String, String> headers) {
    this.headers.putAll(headers);
    return this;
  }

  /**
   * 设置请求头
   *
   * @param header 请求头
   * @param value 值
   * @return 当前下载配置
   */
  public DownloadConfig addHeader(@NonNull String header, String value) {
    this.headers.put(header, value);
    return this;
  }

  /**
   * 设置连接超时时间（毫秒）
   *
   * @param connectTimeout 连接超时时间（毫秒）
   * @return 当前下载配置
   */
  public DownloadConfig setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  /**
   * 获取连接超时时间（毫秒）
   *
   * @return 超时时间
   */
  public int getConnectTimeout() {
    return this.connectTimeout;
  }

  /**
   * 设置读超时时间（毫秒）
   *
   * @param socketTimeout 读超时时间（毫秒）
   * @return 当前下载配置
   */
  public DownloadConfig setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
    return this;
  }

  /**
   * 获取下载超时时间（毫秒）
   *
   * @return 下载超时时间
   */
  public int getSocketTimeout() {
    return this.socketTimeout;
  }

  /**
   * 获取下载代理
   *
   * @return 下载代理
   */
  public ProxyProvider getProxyProvider() {
    return this.proxyProvider;
  }

  /**
   * 设置下载代理提供器
   *
   * @param proxyProvider 代理提供器
   * @return 当前下载配置
   */
  public DownloadConfig setProxyProvider(@NonNull ProxyProvider proxyProvider) {
    this.proxyProvider = proxyProvider;
    return this;
  }

  /**
   * 设置下载默认编码
   *
   * @param charset 编码
   * @return 当前下载配置
   */
  public DownloadConfig setCharset(@NonNull Charset charset) {
    this.charset = charset;
    return this;
  }

  /**
   * 获取默认编码
   *
   * @return 编码
   */
  public Charset getCharset() {
    return this.charset;
  }
}
