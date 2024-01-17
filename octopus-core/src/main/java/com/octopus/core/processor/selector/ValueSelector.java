package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.properties.SelectorProperties;
import com.octopus.core.properties.selector.ValueSelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/16
 */
public class ValueSelector extends AbstractSelector<ValueSelectorProperties> {
    @Override
    protected List<String> doMultiSelect(String source, ValueSelectorProperties selector, Response response) {
        return ListUtil.of(selector.getValue());
    }
}
