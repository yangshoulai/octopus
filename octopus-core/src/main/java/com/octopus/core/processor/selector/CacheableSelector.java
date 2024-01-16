package com.octopus.core.processor.selector;

import cn.hutool.cache.impl.LRUCache;
import cn.hutool.crypto.digest.MD5;
import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;
import com.octopus.core.exception.SelectException;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public abstract class CacheableSelector<T> extends AbstractSelector {
    private final LRUCache<String, T> cache;
    public CacheableSelector() {
        this.cache = new LRUCache<>(16);
    }

    @Override
    protected List<String> doMultiSelect(String source, SelectorProperties selector, Response response)
            throws SelectException {
        try {
            T doc = this.getCacheOrCreate(source);
            if (doc != null) {
                return this.doSelectWithDoc(doc, selector, true, response);
            }
        } catch (Exception e) {
            throw new SelectException(e);
        }
        return null;
    }

    @Override
    protected String doSingleSelect(String source, SelectorProperties selector, Response response)
            throws SelectException {
        try {
            T doc = this.getCacheOrCreate(source);
            if (doc != null) {
                List<String> results = this.doSelectWithDoc(doc, selector, false, response);
                return results == null || results.isEmpty() ? null : results.get(0);
            }
        } catch (Exception e) {
            throw new SelectException(e);
        }
        return null;
    }

    private T getCacheOrCreate(String source) throws Exception {
        String md5 = MD5.create().digestHex(source);
        T doc = this.cache.get(md5);
        if (doc == null) {
            try {
                doc = this.parse(source);
                if (doc != null) {
                    this.cache.put(md5, doc);
                }
            } catch (Exception e) {
                throw new SelectException(e);
            }
        }
        return doc;
    }

    /**
     * 从文档中选择内容
     *
     * @param t        文档
     * @param selector 选择器
     * @param multi    是否多选
     * @param response 请求响应
     * @return 内容
     * @throws SelectException 选取异常
     */
    protected abstract List<String> doSelectWithDoc(
            T t, SelectorProperties selector, boolean multi, Response response) throws SelectException;

    /**
     * 解析文档
     *
     * @param content 内容
     * @return 文档
     * @throws Exception 解析异常
     */
    protected abstract T parse(String content) throws Exception;
}
