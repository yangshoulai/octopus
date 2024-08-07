package com.octopus.core.processor.impl;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.*;
import com.octopus.core.processor.jexl.Jexl;
import com.octopus.core.processor.jexl.JexlContextHolder;
import com.octopus.core.processor.matcher.Matcher;
import com.octopus.core.properties.PropProperties;
import com.octopus.core.properties.processor.ExtractorProperties;
import com.octopus.core.properties.processor.LinkProperties;
import com.octopus.core.properties.processor.ProcessorProperties;
import com.octopus.core.properties.selector.ConverterProperties;
import com.octopus.core.properties.selector.FieldProperties;
import com.octopus.core.utils.RequestHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Slf4j
public class ConfigurableProcessor extends MatchableProcessor {

    private final ExtractorProperties properties;

    private Collector<Map<String, Object>> collector;

    public ConfigurableProcessor(Matcher matcher, ExtractorProperties properties) {
        this(matcher, properties, null);
    }

    public ConfigurableProcessor(Matcher matcher, ExtractorProperties properties, Collector<Map<String, Object>> collector) {
        super(matcher);
        this.properties = properties;
        this.collector = collector;
    }


    @Override
    public void process(Response response, Octopus octopus) {
        Result<Map<String, Object>> result = this.processExtractor(response.asText(), response, properties);
        result.getRequests().forEach(octopus::addRequest);
        JexlContextHolder.getContext().put(JexlContextHolder.KEY_RESULT, result.getObj());
        if (collector != null) {
            collector.collect(result.getObj(), response);
        }
    }

    public ConfigurableProcessor setCollector(Collector<Map<String, Object>> collector) {
        this.collector = collector;
        return this;
    }

    private Result<Map<String, Object>> processExtractor(String content, Response response, ExtractorProperties extractor) {
        Result<Map<String, Object>> result = new Result<>(new HashMap<>(), new ArrayList<>());
        if (extractor != null) {
            if (extractor.getFields() != null) {
                for (FieldProperties field : extractor.getFields()) {
                    this.processField(result, field, content, response);
                }
            }
            if (extractor.getLinks() != null) {
                for (LinkProperties link : extractor.getLinks()) {
                    this.processLink(result, link, content, response);
                }
            }
        }
        return result;
    }

    private void processField(Result<Map<String, Object>> result, FieldProperties field, String content, Response response) {
        List<String> selected = SelectorHelper.getInstance().selectBySelectorProperties(field.getSelector(), content, field.isMulti(), response);
        List<Object> list = new ArrayList<>();
        if (selected != null && !selected.isEmpty()) {
            for (String item : selected) {
                JexlContextHolder.getContext().put(JexlContextHolder.KEY_SELECTED, item);
                if (field.getExtractor() == null) {
                    Converter<?> converter = ConverterRegistry.getInstance().getTypeHandler(field.getType());
                    Object obj = converter.convert(item, field.getConverter() == null ? new ConverterProperties() : field.getConverter());
                    list.add(obj);
                } else {
                    Result<Map<String, Object>> r = processExtractor(item, response, field.getExtractor());
                    if (r.getRequests() != null) {
                        result.getRequests().addAll(r.getRequests());
                    }
                    list.add(r.getObj());
                }
                JexlContextHolder.getContext().remove(JexlContextHolder.KEY_SELECTED);
            }
        }
        if (field.isMulti()) {
            result.getObj().put(field.getName(), list);
        } else if (!list.isEmpty()) {
            result.getObj().put(field.getName(), list.get(0));
        }
    }

    private void processLink(Result<Map<String, Object>> result, LinkProperties link, String content, Response response) {
        List<String> urls = new ArrayList<>();
        if (StrUtil.isNotBlank(link.getUrl())) {
            urls.add(link.getUrl());
        }
        if (link.getSelector() != null) {
            List<String> selected = SelectorHelper.getInstance().selectBySelectorProperties(link.getSelector(), content, true, response);
            if (selected != null) {
                urls.addAll(selected);
            }
        }
        for (String url : urls) {
            JexlContextHolder.getContext().put(JexlContextHolder.KEY_LINK, url);
            if (StrUtil.isNotBlank(url)) {
                Request request =
                        new Request(RequestHelper.completeUrl(response.getRequest().getUrl(), url), link.getMethod())
                                .setPriority(link.getPriority())
                                .setRepeatable(link.isRepeatable());
                link.getHeaders()
                        .forEach(p -> request.addHeader(p.getName(), resolveValueFromProp(content, result.getObj(), p, response)));
                link.getParams()
                        .forEach(p -> request.addParam(p.getName(), resolveValueFromProp(content, result.getObj(), p, response)));
                link.getAttrs()
                        .forEach(p -> request.putAttribute(p.getName(), resolveValueFromProp(content, result.getObj(), p, response)));
                request.setInherit(link.isInherit());
                request.setCache(link.isCache());
                if (link.getBody() != null) {
                    Object r = Jexl.eval(link.getBody());
                    request.setBody(r == null ? new byte[0] : r.toString().getBytes(StandardCharsets.UTF_8));
                }
                result.getRequests().add(request);
            }
            JexlContextHolder.getContext().remove(JexlContextHolder.KEY_LINK);
        }
    }

    private String resolveValueFromProp(String content, Map<String, Object> m, PropProperties prop, Response response) {
        String val = null;
        if (prop.getValue() != null) {
            Object o = Jexl.eval(prop.getValue());
            if (o != null) {
                val = o.toString();
            }
        }

        if (StrUtil.isBlank(val) && StrUtil.isNotBlank(prop.getField()) && m.get(prop.getField()) != null) {
            val = m.get(prop.getField()).toString();
        }
        if (StrUtil.isBlank(val) && prop.getSelector() != null) {
            List<String> selected = SelectorHelper.getInstance().selectBySelectorProperties(prop.getSelector(), content, true, response);
            if (selected != null) {
                val = String.join(",", selected);
            }
        }
        return val;
    }


    public static Processor fromYaml(InputStream inputStream) {
        return ProcessorProperties.fromYaml(inputStream).transform();
    }

    public static Processor fromYaml(String yaml) {
        return ProcessorProperties.fromYaml(yaml).transform();
    }
}
