package com.octopus.core.extractor;

import com.octopus.core.exception.OctopusException;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import com.octopus.core.extractor.selector.CssSelector;
import com.octopus.core.extractor.selector.ISelector;
import com.octopus.core.extractor.selector.XpathSelector;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Slf4j
public class SelectorHelper {

  public static ISelector selector(Selector selector) {
    if (selector.type() == Type.CSS) {
      CssSelector cssSelector = new CssSelector(selector.expression());
      cssSelector.setAttr(selector.attr());
      cssSelector.setMulti(selector.multi());
      cssSelector.setFilter(selector.filter());
      cssSelector.setSelf(selector.self());
      return cssSelector;
    } else if (selector.type() == Type.XPATH) {
      XpathSelector xpathSelector = new XpathSelector(selector.expression());
      xpathSelector.setFilter(selector.filter());
      xpathSelector.setMulti(selector.multi());
      xpathSelector.setAttr(selector.attr());
      return xpathSelector;
    }
    throw new OctopusException(
        String.format("No selector handler found for select [%s]", selector));
  }
}
