package com.octopus.core.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.NonNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Data
public class CssSelector implements ISelector {

  private String selector;

  private String attr;

  private boolean multi = true;

  private boolean filter = true;

  private boolean self = true;

  public CssSelector(@NonNull String selector) {
    this(selector, null);
  }

  public CssSelector(@NonNull String selector, String attr) {
    this.attr = attr;
    this.selector = selector;
  }

  @Override
  public List<String> select(String content) {
    Document document = Jsoup.parse(content);
    Elements elements = document.select(this.selector);
    Stream<String> stream =
        elements.stream()
            .map(
                e -> {
                  if (StrUtil.isNotBlank(attr)) {
                    return e.attr(attr);
                  } else if (this.self) {
                    return e.toString();
                  } else {
                    return e.html();
                  }
                })
            .filter(s -> !this.filter || StrUtil.isNotBlank(s));
    if (this.multi) {
      return stream.collect(Collectors.toList());
    }
    String result = stream.filter(StrUtil::isNotBlank).findFirst().orElse(null);
    return result == null ? new ArrayList<>() : ListUtil.toList(result);
  }
}
