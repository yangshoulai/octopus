package com.octopus.core.downloader.proxy;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Request;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class PollingProxyProvider implements ProxyProvider {

  private final List<Proxy> proxies;

  private final AtomicInteger index = new AtomicInteger(0);

  public PollingProxyProvider(@NonNull Proxy... proxies) {
    this.proxies = ListUtil.toList(proxies);
  }

  public PollingProxyProvider(@NonNull List<Proxy> proxies) {
    this.proxies = proxies;
  }

  @Override
  public Proxy provide(Request request) {
    if (proxies.isEmpty()) {
      return Proxy.NO_PROXY;
    }
    int i =
        index.getAndUpdate(
            pre -> {
              if (pre >= this.proxies.size() - 1) {
                return 0;
              } else {
                return pre + 1;
              }
            });
    return this.proxies.get(i);
  }
}
