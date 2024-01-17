package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import com.octopus.core.Response;
import com.octopus.core.properties.SelectorProperties;
import com.octopus.core.properties.selector.ParamSelectorProperties;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class ParamSelector extends AbstractSelector<ParamSelectorProperties> {
    @Override
    public List<String> doMultiSelect(String source, ParamSelectorProperties selector, Response response) {
        String result = response.getRequest().getParams().get(selector.getName());
        if (result == null) {
            CharSequence paramValue =
                    UrlBuilder.of(response.getRequest().getUrl()).getQuery().get(selector.getName());
            if (paramValue != null) {
                result = paramValue.toString();
            }
        }
        return result == null ? null : ListUtil.of(result);
    }
}
