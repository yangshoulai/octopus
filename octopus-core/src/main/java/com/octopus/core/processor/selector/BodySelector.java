package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.CharsetUtil;
import com.octopus.core.Response;
import com.octopus.core.properties.selector.BodySelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/15
 */
public class BodySelector extends AbstractSelector<BodySelectorProperties> {
    @Override
    protected List<String> doMultiSelect(String source, BodySelectorProperties selector, Response response) {
        return ListUtil.of(new String(response.getBody(), CharsetUtil.CHARSET_UTF_8));
    }


}
