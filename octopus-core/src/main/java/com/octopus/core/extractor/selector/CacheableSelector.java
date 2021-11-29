package com.octopus.core.extractor.selector;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.octopus.core.extractor.annotation.Selector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public abstract class CacheableSelector<T> implements ISelector {

  private final LRUCache<String, T> cache;

  public CacheableSelector() {
    this.cache = CacheUtil.newLRUCache(10);
  }

  @Override
  public final List<String> select(String content, Selector selector) {
    String md5 = MD5.create().digestHex(content);
    T doc = this.cache.get(md5);
    if (doc == null) {
      doc = this.parse(content);
      if (doc != null) {
        this.cache.put(md5, doc);
      }
    }
    if (doc != null) {
      List<String> results = this.selectWithType(doc, selector);
      if (results != null) {
        if (selector.trim()) {
          results = results.stream().map(StrUtil::trim).collect(Collectors.toList());
        }
        Stream<String> stream = results.stream();
        if (selector.multi()) {
          return stream.collect(Collectors.toList());
        }
        String result = stream.filter(StrUtil::isNotBlank).findFirst().orElse(null);
        return result == null ? new ArrayList<>() : ListUtil.toList(result);
      }
    }
    return Collections.emptyList();
  }

  protected abstract List<String> selectWithType(T t, Selector selector);

  protected abstract T parse(String content);
}
