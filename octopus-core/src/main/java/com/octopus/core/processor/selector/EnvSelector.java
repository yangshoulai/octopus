package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.properties.selector.EnvSelectorProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/19
 */
public class EnvSelector extends AbstractSelector<EnvSelectorProperties> {
    @Override
    protected List<String> doMultiSelect(String source, EnvSelectorProperties selector, Response response) {
        String e = System.getProperty(selector.getName());
        if (StrUtil.isBlank(e)) {
            e = System.getenv(selector.getName());
        }
        return e == null ? new ArrayList<>() : ListUtil.of(e);
    }
}
