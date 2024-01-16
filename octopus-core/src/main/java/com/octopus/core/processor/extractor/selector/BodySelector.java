package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.CharsetUtil;
import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/15
 */
public class BodySelector extends AbstractSelector {
    @Override
    protected List<String> doMultiSelect(String source, SelectorProperties selector, Response response) {
        return ListUtil.of(new String(response.getBody(), CharsetUtil.CHARSET_UTF_8));
    }
}
