package com.octopus.core.processor.extractor.selector;

import com.octopus.core.Response;
import com.octopus.core.configurable.FormatterProperties;
import com.octopus.core.configurable.SelectorProperties;
import com.octopus.core.processor.extractor.annotation.Selector;
import com.octopus.core.exception.SelectorNotFoundException;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public class FieldSelectorRegistry {


    private final Map<Selector.Type, FieldSelector> handlers = new HashMap<>();

    private FieldSelectorRegistry() {
        registerHandler(Selector.Type.Url, new UrlFieldSelector());
        registerHandler(Selector.Type.Attr, new AttrFieldSelector());
        registerHandler(Selector.Type.Param, new ParamFieldSelector());
        registerHandler(Selector.Type.Header, new HeaderFieldSelector());
        registerHandler(Selector.Type.Json, new JsonFieldSelector());
        registerHandler(Selector.Type.Css, new CssFieldSelector());
        registerHandler(Selector.Type.Xpath, new XpathFieldSelector());
        registerHandler(Selector.Type.Regex, new RegexFieldSelector());
        registerHandler(Selector.Type.None, new NoneFieldSelector());
    }

    public static FieldSelectorRegistry getInstance() {
        return FieldSelectorRegistry.Holder.INSTANCE;
    }

    private static class Holder {
        public static final FieldSelectorRegistry INSTANCE = new FieldSelectorRegistry();
    }

    public FieldSelector getSelectorHandler(@NonNull Selector.Type type) throws SelectorNotFoundException {
        FieldSelector handler = handlers.get(type);
        if (handler == null) {
            throw new SelectorNotFoundException("Selector not found for type " + type);
        }
        return handler;
    }

    public void registerHandler(@NonNull Selector.Type type, @NonNull FieldSelector selector) {
        handlers.put(type, selector);
    }

    public List<String> select(Selector selector, String source, boolean multi, Response response) {
        FieldSelector fs = getSelectorHandler(selector.type());
        SelectorProperties properties = new SelectorProperties(selector.type());
        properties.setValue(selector.value());
        properties.setDef(selector.def());
        properties.setSelf(selector.self());
        properties.setAttr(selector.attr());
        properties.setFormat(selector.format());
        properties.setGroups(selector.groups());
        properties.setNode(selector.node());
        if (selector.formatter() != null) {
            FormatterProperties formatter = new FormatterProperties();
            formatter.setSeparator(selector.formatter().separator());
            formatter.setFilter(selector.formatter().filter());
            formatter.setTrim(selector.formatter().trim());
            formatter.setSplit(selector.formatter().split());
            formatter.setRegex(selector.formatter().regex());
            formatter.setFormat(selector.formatter().format());
            formatter.setGroups(selector.formatter().groups());
            properties.setFormatter(formatter);
        }
        return fs.select(source, multi, properties, response);
    }
}
