package com.octopus.core.downloader.proxy;

import com.octopus.core.Request;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class DefaultProxyProvider implements ProxyProvider {
    @Override
    public HttpProxy provide(Request request) {
        return HttpProxy.PROXY_DIRECT;
    }
}
