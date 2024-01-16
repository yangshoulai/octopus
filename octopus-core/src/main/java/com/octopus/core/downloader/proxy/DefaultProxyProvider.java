package com.octopus.core.downloader.proxy;

import com.octopus.core.Request;

import java.net.Proxy;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class DefaultProxyProvider implements ProxyProvider {
    @Override
    public Proxy provide(Request request) {
        return Proxy.NO_PROXY;
    }
}
