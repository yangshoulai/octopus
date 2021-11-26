package com.octopus.core.extractor;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.format.RegexFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class LinkHelper {

  public static List<Request> parse(String content, Link link) {
    List<Request> requests = new ArrayList<>();
    List<String> selected = SelectorHelper.select(content, link.selector());
    if (selected != null && !selected.isEmpty()) {
      RegexFormat[] formats = link.formats();
      if (formats != null) {
        for (String url : selected) {
          url = FormatterHelper.format(url, formats);
          if (StrUtil.isNotBlank(url)) {
            requests.add(
                new Request(url, link.method())
                    .setPriority(link.priority())
                    .setRepeatable(link.repeatable()));
          }
        }
      }
    }
    return requests;
  }
}
