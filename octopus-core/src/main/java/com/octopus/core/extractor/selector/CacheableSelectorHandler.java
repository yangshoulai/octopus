package com.octopus.core.extractor.selector;

import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public abstract class CacheableSelectorHandler<T, A extends Annotation>
    implements SelectorHandler<A> {

  private final LRUCache<String, T> cache;

  public CacheableSelectorHandler() {
    this.cache = new LRUCache<>(16);
  }

  @Override
  public final List<String> select(String content, A selector) throws Exception {
    String md5 = MD5.create().digestHex(content);
    T doc = this.cache.get(md5);
    if (doc == null) {
      doc = this.parse(content);
      if (doc != null) {
        this.cache.put(md5, doc);
      }
    }
    return this.selectWithType(doc, selector);
  }

  protected abstract List<String> selectWithType(T t, A selector) throws Exception;

  protected abstract T parse(String content) throws Exception;

  protected List<String> filterResults(
      List<String> results, boolean filter, boolean trim, boolean multi) {
    List<String> list =
        results.stream()
            .filter(s -> !filter || StrUtil.isNotBlank(s))
            .map(s -> trim ? StrUtil.trim(s) : s)
            .collect(Collectors.toList());
    if (multi) {
      return list;
    }
    return list.isEmpty() ? list : ListUtil.toList(list.get(0));
  }
}
