package com.octopus.core.processor.extractor.selector;

import cn.hutool.json.JSONUtil;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;
import net.minidev.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
public class JsonFieldSelector extends CacheableFieldSelector<String> {

    private static final Configuration CONFIGURATION =
            Configuration.builder()
                    .options(Option.ALWAYS_RETURN_LIST)
                    .options(Option.SUPPRESS_EXCEPTIONS)
                    .build();

    @Override
    public List<String> doSelectWithDoc(
            String json, SelectorProperties selector, boolean multi, Response response) {
        List<String> list = new ArrayList<>();
        Object obj = JsonPath.using(CONFIGURATION).parse(json).read(selector.getValue());
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            for (Object o : array) {
                if (o != null) {
                    if (o instanceof Map || o instanceof List) {
                        list.add(JSONUtil.toJsonStr(o));
                    } else {
                        list.add(o.toString());
                    }
                }
            }
        }
        return list;
    }

    @Override
    protected String parse(String content) {
        return content;
    }
}
