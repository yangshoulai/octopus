package com.octopus.core.processor.configurable.selector;

import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.extractor.selector.SelectorNotFoundException;
import lombok.NonNull;

import java.util.HashMap;
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
}
