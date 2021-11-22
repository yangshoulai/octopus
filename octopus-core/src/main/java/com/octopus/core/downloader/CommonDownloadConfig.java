package com.octopus.core.downloader;

import cn.hutool.core.util.CharsetUtil;
import com.octopus.core.downloader.proxy.DefaultProxyProvider;
import com.octopus.core.downloader.proxy.ProxyProvider;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class CommonDownloadConfig implements DownloadConfig {

  private Map<String, String> headers = new HashMap<>();

  private int connectTimeout = 60000;

  private int socketTimeout = 60000;

  private ProxyProvider proxyProvider = new DefaultProxyProvider();

  private Charset charset = CharsetUtil.CHARSET_UTF_8;

  public CommonDownloadConfig() {}

  @Override
  public Map<String, String> getHeaders() {
    return this.headers;
  }

  @Override
  public int getConnectTimeout() {
    return this.connectTimeout;
  }

  @Override
  public int getSocketTimeout() {
    return this.socketTimeout;
  }

  @Override
  public ProxyProvider getProxyProvider() {
    return proxyProvider;
  }

  @Override
  public Charset getCharset() {
    return charset;
  }

  public CommonDownloadConfig setHeaders(Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public CommonDownloadConfig setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public CommonDownloadConfig setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
    return this;
  }

  public CommonDownloadConfig setProxyProvider(ProxyProvider proxyProvider) {
    this.proxyProvider = proxyProvider;
    return this;
  }

  public void setCharset(@NonNull Charset charset) {
    this.charset = charset;
  }
}
