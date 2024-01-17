package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.properties.selector.AttrSelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public class AttrSelector extends AbstractSelector<AttrSelectorProperties> {
    @Override
    protected List<String> doMultiSelect(String source, AttrSelectorProperties selector, Response response) {
        Object attr = response.getRequest().getAttribute(selector.getName());
        String val = attr == null ? null : attr.toString();
        return val == null ? null : ListUtil.of(val);
    }

}
