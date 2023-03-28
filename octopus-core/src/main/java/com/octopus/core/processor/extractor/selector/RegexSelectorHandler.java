package com.octopus.core.processor.extractor.selector;

import com.octopus.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
public class RegexSelectorHandler extends CacheableSelectorHandler<String> {

  @Override
  protected List<String> doSelectWithDoc(
      String content, Selector selector, boolean multi, Response response) {
    List<String> list = new ArrayList<>();
    int[] groups = selector.groups();
    String format = selector.format();
    Pattern pattern = Pattern.compile(selector.value());
    Matcher matcher = pattern.matcher(content);
    while (matcher.find()) {
      List<String> args = new ArrayList<>();
      for (int group : groups) {
        args.add(matcher.group(group));
      }
      list.add(String.format(format, args.toArray()));
      if (!multi) {
        break;
      }
    }
    return list;
  }

  @Override
  protected String parse(String content) {
    return content;
  }
}
