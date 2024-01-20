package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.properties.selector.IndexSelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/19
 */
public class IndexSelector extends AbstractSelector<IndexSelectorProperties> {
    @Override
    protected List<String> doMultiSelect(String source, IndexSelectorProperties selector, Response response) {
        return ListUtil.of(String.valueOf(response.getRequest().getIndex()));
    }
}
