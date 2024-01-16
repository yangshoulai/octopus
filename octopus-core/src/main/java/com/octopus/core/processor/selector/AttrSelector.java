package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public class AttrSelector extends AbstractSelector {
    @Override
    protected List<String> doMultiSelect(String source, SelectorProperties selector, Response response) {
        Object attr = response.getRequest().getAttribute(selector.getValue());
        String val = attr == null ? null : attr.toString();
        return val == null ? null : ListUtil.of(val);
    }
}
