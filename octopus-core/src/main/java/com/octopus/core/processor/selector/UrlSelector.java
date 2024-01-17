package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.SelectException;
import com.octopus.core.properties.selector.UrlSelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class UrlSelector extends AbstractSelector<UrlSelectorProperties> {
    @Override
    public List<String> doMultiSelect(String content, UrlSelectorProperties selector, Response response)
            throws SelectException {
        return ListUtil.of(response.getRequest().getUrl());
    }
}
