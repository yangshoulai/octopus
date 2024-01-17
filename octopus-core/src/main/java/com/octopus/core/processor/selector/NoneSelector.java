package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.SelectException;
import com.octopus.core.properties.selector.NoneSelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/4
 */
public class NoneSelector extends AbstractSelector<NoneSelectorProperties> {
    @Override
    protected List<String> doMultiSelect(String source, NoneSelectorProperties selector, Response response)
            throws SelectException {
        return ListUtil.empty();
    }

    @Override
    protected String doSingleSelect(String source, NoneSelectorProperties selector, Response response)
            throws SelectException {
        return null;
    }
}
