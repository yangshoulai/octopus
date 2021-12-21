package com.octopus.core.test;

import cn.hutool.core.lang.UUID;
import com.octopus.core.utils.LRUCache;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/21
 */
public class LRUCacheTests {

  public static void main(String[] args) {
    LRUCache<String, LRUCacheTests> cache = new LRUCache<>(10);

    new Thread(
            () -> {
              while (true) {
                String id = UUID.fastUUID().toString();
                LRUCacheTests t = cache.get(id);
                if (t == null) {
                  cache.put(id, new LRUCacheTests());
                }
                System.out.println(cache.size());
              }
            })
        .start();
    new Thread(
            () -> {
              while (true) {
                String id = UUID.fastUUID().toString();
                LRUCacheTests t = cache.get(id);
                if (t == null) {
                  cache.put(id, new LRUCacheTests());
                }
                System.out.println(cache.size());
              }
            })
        .start();
  }
}
