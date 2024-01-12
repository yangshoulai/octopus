package com.octopus.core.processor.configurable.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.processor.configurable.SelectorProperties;
import com.octopus.core.processor.extractor.selector.SelectException;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/4
 */
public class NoneFieldSelector extends AbstractFieldSelector {

    @Override
    protected List<String> doMultiSelect(String source, SelectorProperties selector, Response response)
            throws SelectException {
        return ListUtil.empty();
    }

    @Override
    protected String doSingleSelect(String source, SelectorProperties selector, Response response)
            throws SelectException {
        return null;
    }
}
