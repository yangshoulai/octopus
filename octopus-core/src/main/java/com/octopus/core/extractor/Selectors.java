package com.octopus.core.extractor;

import com.octopus.core.exception.OctopusException;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import com.octopus.core.extractor.selector.CssSelector;
import com.octopus.core.extractor.selector.ISelector;
import com.octopus.core.extractor.selector.JsonSelector;
import com.octopus.core.extractor.selector.RegexSelector;
import com.octopus.core.extractor.selector.XpathSelector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Slf4j
public class Selectors {

  private static final Map<Type, ISelector> SELECTORS = new HashMap<>();

  static {
    SELECTORS.put(Type.CSS, new CssSelector());
    SELECTORS.put(Type.XPATH, new XpathSelector());
    SELECTORS.put(Type.JSON, new JsonSelector());
    SELECTORS.put(Type.REGEX, new RegexSelector());
  }

  static List<String> select(String content, Selector selector) {
    if (!SELECTORS.containsKey(selector.type())) {
      throw new OctopusException("No selector found for type " + selector.type());
    }
    return SELECTORS.get(selector.type()).select(content, selector);
  }
}
