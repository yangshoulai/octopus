package com.octopus.core.downloader;

import com.octopus.core.Request;
import com.octopus.core.downloader.proxy.ProxyProvider;
import java.net.Proxy;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/27
 */
public abstract class AbstractDownloader implements Downloader {

  protected Proxy resolveProxy(ProxyProvider proxyProvider, Request request) {
    Proxy proxy = proxyProvider == null ? null : proxyProvider.provide(request);
    return proxy == null ? Proxy.NO_PROXY : proxy;
  }
}
