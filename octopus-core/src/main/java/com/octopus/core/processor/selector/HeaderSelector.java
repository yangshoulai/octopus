package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.properties.selector.HeaderSelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class HeaderSelector extends AbstractSelector<HeaderSelectorProperties> {
    @Override
    public List<String> doMultiSelect(String source, HeaderSelectorProperties selector, Response response) {
        String result = response.getHeaders().get(selector.getName());
        return result == null ? null : ListUtil.of(result);
    }
}
