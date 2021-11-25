package com.octopus.core.extractor;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.extractor.annotation.Format;
import com.octopus.core.extractor.annotation.Link;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class LinkHelper {

  public static List<Request> parse(String content, Link link) {
    List<Request> requests = new ArrayList<>();
    List<String> selected = SelectorHelper.selector(link.selector()).select(content);
    if (selected != null && !selected.isEmpty()) {
      Format[] formats = link.formats();
      if (formats != null) {
        for (String s : selected) {
          for (Format format : formats) {
            s = FormatHelper.format(s, format);
          }
          if (StrUtil.isNotBlank(s)) {
            requests.add(
                new Request(s, link.method())
                    .setPriority(link.priority())
                    .setRepeatable(link.repeatable()));
          }
        }
      }
    }
    return requests;
  }
}
