package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/16
 */
public class ValueSelector extends AbstractSelector {
    @Override
    protected List<String> doMultiSelect(String source, SelectorProperties selector, Response response) {
        return ListUtil.of(selector.getValue());
    }
}
