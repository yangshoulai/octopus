package com.octopus.core.downloader.proxy;

import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/21
 */
@Getter
public class HttpProxy {

    public static HttpProxy PROXY_DIRECT = new HttpProxy();

    private Proxy.Type type = Proxy.Type.DIRECT;

    @Setter
    private String host = "127.0.0.1";

    @Setter
    private int port = 80;

    @Setter
    private String username;

    @Setter
    private String password;

    private HttpProxy() {

    }

    public HttpProxy(Proxy.Type type, String host, int port) {
        if (type == Proxy.Type.DIRECT) {
            throw new IllegalArgumentException("type " + type + " can init");
        }
        this.type = type;
        this.host = host;
        this.port = port;
    }

    public static HttpProxy from(Proxy proxy) {
        if (proxy.type() == Proxy.Type.DIRECT || proxy == Proxy.NO_PROXY) {
            return PROXY_DIRECT;
        }
        HttpProxy httpProxy = new HttpProxy();
        httpProxy.type = proxy.type();
        if (proxy.address() != null) {
            InetSocketAddress addr = (InetSocketAddress) proxy.address();
            httpProxy.host = addr.getHostName();
            httpProxy.port = addr.getPort();
        }
        return httpProxy;
    }

    public Proxy to() {
        if (type == null || type == Proxy.Type.DIRECT) {
            return Proxy.NO_PROXY;
        }
        return new Proxy(type, new InetSocketAddress(host, port));
    }


    @Override
    public String toString() {
        return this == HttpProxy.PROXY_DIRECT ? "DIRECT" : this.host + ":" + this.port;
    }
}
