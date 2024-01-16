package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class HeaderSelector extends AbstractSelector {
    @Override
    public List<String> doMultiSelect(String source, SelectorProperties selector, Response response) {
        String result = response.getHeaders().get(selector.getValue());
        return result == null ? null : ListUtil.of(result);
    }
}
