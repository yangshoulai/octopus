package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class ParamSelector extends AbstractSelector {

    @Override
    public List<String> doMultiSelect(String source, SelectorProperties selector, Response response) {
        String result = response.getRequest().getParams().get(selector.getValue());
        if (result == null) {
            CharSequence paramValue =
                    UrlBuilder.of(response.getRequest().getUrl()).getQuery().get(selector.getValue());
            if (paramValue != null) {
                result = paramValue.toString();
            }
        }
        return result == null ? null : ListUtil.of(result);
    }
}
