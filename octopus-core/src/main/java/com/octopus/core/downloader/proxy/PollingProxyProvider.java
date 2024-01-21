package com.octopus.core.downloader.proxy;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Request;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class PollingProxyProvider implements ProxyProvider {
    private final List<HttpProxy> proxies;

    private final AtomicInteger index = new AtomicInteger(0);

    private final IntUnaryOperator updater;

    public PollingProxyProvider(@NonNull HttpProxy... proxies) {
        this(ListUtil.toList(proxies));
    }

    public PollingProxyProvider(@NonNull List<HttpProxy> proxies) {
        this.proxies = proxies;
        this.updater =
                pre -> {
                    if (pre >= this.proxies.size() - 1) {
                        return 0;
                    } else {
                        return pre + 1;
                    }
                };
    }

    @Override
    public HttpProxy provide(Request request) {
        if (proxies.isEmpty()) {
            return HttpProxy.PROXY_DIRECT;
        }
        int i = index.getAndUpdate(this.updater);
        return this.proxies.get(i);
    }
}
