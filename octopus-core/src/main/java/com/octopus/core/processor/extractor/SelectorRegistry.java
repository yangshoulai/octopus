package com.octopus.core.processor.extractor;

import com.octopus.core.Response;
import com.octopus.core.configurable.FormatterProperties;
import com.octopus.core.configurable.SelectorProperties;
import com.octopus.core.exception.SelectorNotFoundException;
import com.octopus.core.processor.extractor.selector.*;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public class SelectorRegistry {


    private final Map<com.octopus.core.processor.extractor.annotation.Selector.Type, Selector> handlers = new HashMap<>();

    private SelectorRegistry() {
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Url, new UrlSelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Attr, new AttrSelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Param, new ParamSelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Header, new HeaderSelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Json, new JsonSelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Css, new CssSelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Xpath, new XpathSelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Regex, new RegexSelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Body, new BodySelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.Value, new ValueSelector());
        registerHandler(com.octopus.core.processor.extractor.annotation.Selector.Type.None, new NoneSelector());
    }

    public static SelectorRegistry getInstance() {
        return SelectorRegistry.Holder.INSTANCE;
    }

    private static class Holder {
        public static final SelectorRegistry INSTANCE = new SelectorRegistry();
    }

    public Selector getSelectorHandler(@NonNull com.octopus.core.processor.extractor.annotation.Selector.Type type) throws SelectorNotFoundException {
        Selector handler = handlers.get(type);
        if (handler == null) {
            throw new SelectorNotFoundException("Selector not found for type " + type);
        }
        return handler;
    }

    public void registerHandler(@NonNull com.octopus.core.processor.extractor.annotation.Selector.Type type, @NonNull Selector selector) {
        handlers.put(type, selector);
    }

    public List<String> select(SelectorProperties selector, String source, boolean multi, Response response) {
        Selector fs = getSelectorHandler(selector.getType());
        return fs.select(source, multi, selector, response);
    }

    public List<String> select(com.octopus.core.processor.extractor.annotation.Selector selector, String source, boolean multi, Response response) {
        Selector fs = getSelectorHandler(selector.type());
        SelectorProperties properties = new SelectorProperties(selector.type());
        properties.setValue(selector.value());
        properties.setDef(selector.def());
        properties.setSelf(selector.self());
        properties.setAttr(selector.attr());
        properties.setFormat(selector.format());
        properties.setGroups(selector.groups());
        properties.setNode(selector.node());
        if (selector.formatter() != null) {
            FormatterProperties formatter = getFormatterProperties(selector);
            properties.setFormatter(formatter);
        }
        return select(properties, source, multi, response);
    }

    @NotNull
    private static FormatterProperties getFormatterProperties(com.octopus.core.processor.extractor.annotation.Selector selector) {
        FormatterProperties formatter = new FormatterProperties();
        formatter.setSeparator(selector.formatter().separator());
        formatter.setFilter(selector.formatter().filter());
        formatter.setTrim(selector.formatter().trim());
        formatter.setSplit(selector.formatter().split());
        formatter.setRegex(selector.formatter().regex());
        formatter.setFormat(selector.formatter().format());
        formatter.setGroups(selector.formatter().groups());
        return formatter;
    }
}
