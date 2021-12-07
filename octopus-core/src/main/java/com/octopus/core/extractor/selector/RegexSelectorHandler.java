package com.octopus.core.extractor.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
public class RegexSelectorHandler extends CacheableSelector<String, RegexSelector> {

  @Override
  protected List<String> selectWithType(String content, RegexSelector selector) {
    List<String> list = new ArrayList<>();
    int[] groups = selector.groups();
    String format = selector.format();
    Pattern pattern = Pattern.compile(selector.expression());
    Matcher matcher = pattern.matcher(content);
    while (matcher.find()) {
      List<String> args = new ArrayList<>();
      for (int group : groups) {
        args.add(matcher.group(group));
      }
      list.add(String.format(format, args.toArray()));
    }
    return filterResults(list, selector.filter(), selector.trim(), selector.multi());
  }

  @Override
  protected String parse(String content) {
    return content;
  }
}
