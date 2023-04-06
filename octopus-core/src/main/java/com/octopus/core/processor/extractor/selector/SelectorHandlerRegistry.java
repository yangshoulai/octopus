package com.octopus.core.processor.extractor.selector;

import com.octopus.core.processor.extractor.selector.Selector.Type;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class SelectorHandlerRegistry {

  private final Map<Type, SelectorHandler> handlers = new HashMap<>();

  private SelectorHandlerRegistry() {
    registerHandler(Type.Url, new UrlSelectorHandler());
    registerHandler(Type.Attr, new AttrSelectorHandler());
    registerHandler(Type.Param, new ParamSelectorHandler());
    registerHandler(Type.Json, new JsonSelectorHandler());
    registerHandler(Type.Css, new CssSelectorHandler());
    registerHandler(Type.Xpath, new XpathSelectorHandler());
    registerHandler(Type.Regex, new RegexSelectorHandler());
    registerHandler(Type.None, new NoneSelectorHandler());
  }

  public static SelectorHandlerRegistry getInstance() {
    return Holder.INSTANCE;
  }

  private static class Holder {
    public static final SelectorHandlerRegistry INSTANCE = new SelectorHandlerRegistry();
  }

  public SelectorHandler getSelectorHandler(@NonNull Type type) throws SelectorNotFoundException {
    SelectorHandler handler = handlers.get(type);
    if (handler == null) {
      throw new SelectorNotFoundException("Selector not found for type " + type);
    }
    return handler;
  }

  public void registerHandler(@NonNull Type type, @NonNull SelectorHandler selectorHandler) {
    handlers.put(type, selectorHandler);
  }
}
