package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;
import com.octopus.core.exception.SelectException;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class UrlSelector extends AbstractSelector {
    @Override
    public List<String> doMultiSelect(String content, SelectorProperties selector, Response response)
            throws SelectException {
        return ListUtil.of(response.getRequest().getUrl());
    }
}
