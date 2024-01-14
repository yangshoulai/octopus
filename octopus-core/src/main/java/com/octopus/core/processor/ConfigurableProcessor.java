package com.octopus.core.processor;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.Collector;
import com.octopus.core.processor.extractor.Result;
import com.octopus.core.processor.extractor.configurable.*;
import com.octopus.core.processor.extractor.convert.TypeConverter;
import com.octopus.core.processor.extractor.convert.TypeConverterRegistry;
import com.octopus.core.processor.extractor.selector.FieldSelector;
import com.octopus.core.processor.extractor.selector.FieldSelectorRegistry;
import com.octopus.core.processor.matcher.Matcher;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

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
        FieldSelector selector = FieldSelectorRegistry.getInstance().getSelectorHandler(field.getSelector().getType());
        List<String> selected = selector.select(content, field.isMulti(), field.getSelector(), response);
        List<Object> list = new ArrayList<>();
        if (selected != null && !selected.isEmpty()) {
            for (String item : selected) {
                if (field.getExtractor() == null) {
                    TypeConverter<?> converter = TypeConverterRegistry.getInstance().getTypeHandler(field.getType());
                    Object obj = converter.convert(item, field.getExt() == null ? new FieldExtProperties() : field.getExt());
                    list.add(obj);
                } else {
                    Result<Map<String, Object>> r = processExtractor(item, response, field.getExtractor());
                    if (r.getRequests() != null) {
                        result.getRequests().addAll(r.getRequests());
                    }
                    list.add(r.getObj());
                }
            }
        }
        if (field.isMulti()) {
            result.getObj().put(field.getName(), list);
        } else if (!list.isEmpty()) {
            result.getObj().put(field.getName(), list.get(0));
        }
    }

    private void processLink(Result<Map<String, Object>> result, LinkProperties link, String content, Response response) {
        Set<String> urls = new HashSet<>();
        if (StrUtil.isNotBlank(link.getUrl())) {
            urls.add(link.getUrl());
        }
        FieldSelector selector =
                FieldSelectorRegistry.getInstance().getSelectorHandler(link.getSelector().getType());
        List<String> selected = selector.select(content, true, link.getSelector(), response);
        if (selected != null) {
            urls.addAll(selected);
        }
        for (String url : urls) {
            if (StrUtil.isNotBlank(url)) {
                Request request =
                        new Request(completeUrl(response.getRequest().getUrl(), url), link.getMethod())
                                .setPriority(link.getPriority())
                                .setRepeatable(link.isRepeatable());
                link.getHeaders()
                        .forEach(p -> request.addHeader(p.getName(), resolveValueFromProp(result.getObj(), p)));
                link.getParams()
                        .forEach(p -> request.addParam(p.getName(), resolveValueFromProp(result.getObj(), p)));
                link.getAttrs()
                        .forEach(p -> request.putAttribute(p.getName(), resolveValueFromProp(result.getObj(), p)));
                request.setInherit(link.isInherit());
                result.getRequests().add(request);
            }
        }
    }

    private String resolveValueFromProp(Map<String, Object> m, PropProperties prop) {
        if (StrUtil.isNotBlank(prop.getField())) {
            Object val = m.get(prop.getField());
            return val == null ? null : val.toString();
        }
        return prop.getValue();
    }

    private String completeUrl(String currentUrl, String url) {
        if (!HttpUtil.isHttp(url) && !HttpUtil.isHttps(url)) {
            if (url.startsWith("/")) {
                return URLUtil.completeUrl(currentUrl, url);
            } else {
                url =
                        UrlBuilder.of(currentUrl).setQuery(UrlQuery.of(url, CharsetUtil.CHARSET_UTF_8)).build();
            }
        }
        return url;
    }
}
