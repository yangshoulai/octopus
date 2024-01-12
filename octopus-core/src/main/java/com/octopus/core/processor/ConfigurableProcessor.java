package com.octopus.core.processor;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.configurable.*;
import com.octopus.core.processor.configurable.convert.TypeConverter;
import com.octopus.core.processor.configurable.convert.TypeConverterRegistry;
import com.octopus.core.processor.configurable.selector.FieldSelector;
import com.octopus.core.processor.configurable.selector.FieldSelectorRegistry;
import com.octopus.core.processor.extractor.Collector;
import com.octopus.core.processor.extractor.Result;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Slf4j
public class ConfigurableProcessor extends AbstractConfigurableProcessor {

    private final ProcessorProperties properties;

    public ConfigurableProcessor(ProcessorProperties properties) {
        this.properties = properties;
    }


    @Override
    public void process(Response response, Octopus octopus) {
        Result<Map<String, Object>> result = this.processExtractor(response.asText(), response, properties.getExtractor());
        result.getRequests().forEach(octopus::addRequest);
        // TODO 收集
        log.info(JSONUtil.toJsonPrettyStr(result.getObj()));
    }

}
