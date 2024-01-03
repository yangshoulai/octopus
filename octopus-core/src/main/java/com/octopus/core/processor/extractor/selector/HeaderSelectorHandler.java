package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class HeaderSelectorHandler extends AbstractSelectorHandler {

    @Override
    public List<String> doMultiSelect(String source, Selector selector, Response response) {
        String result = response.getHeaders().get(selector.value());
        return result == null ? null : ListUtil.of(result);
    }
}
