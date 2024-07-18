package com.octopus.core.downloader;

import com.octopus.core.Request;
import com.octopus.core.downloader.proxy.HttpProxy;
import com.octopus.core.downloader.proxy.ProxyProvider;

import java.util.Properties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/27
 */
public abstract class AbstractCustomDownloader implements Downloader {
    protected final Properties conf;

    public AbstractCustomDownloader(Properties conf) {
        this.conf = conf;
    }

    protected HttpProxy resolveProxy(ProxyProvider proxyProvider, Request request) {
        HttpProxy proxy = proxyProvider == null ? null : proxyProvider.provide(request);
        return proxy == null ? HttpProxy.PROXY_DIRECT : proxy;
    }
}
