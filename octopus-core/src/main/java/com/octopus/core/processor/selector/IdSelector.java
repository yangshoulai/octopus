package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.properties.selector.IdSelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/19
 */
public class IdSelector extends AbstractSelector<IdSelectorProperties> {
    @Override
    protected List<String> doMultiSelect(String source, IdSelectorProperties selector, Response response) {
        return ListUtil.of(response.getRequest().getId());
    }
}
