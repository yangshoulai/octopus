package com.octopus.core.extractor.selector;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CssSelectorHandler extends CacheableSelector<Document, CssSelector> {

  @Override
  public List<String> selectWithType(Document document, CssSelector selector) {
    Elements elements = document.select(selector.expression());
    List<String> selected =
        elements.stream()
            .map(
                e -> {
                  if (StrUtil.isNotBlank(selector.attr())) {
                    return e.attr(selector.attr());
                  } else if (selector.self()) {
                    return XmlUtil.format(e.toString());
                  } else {
                    return e.html();
                  }
                })
            .filter(s -> !selector.filter() || StrUtil.isNotBlank(s))
            .map(s -> selector.trim() ? StrUtil.trim(s) : s)
            .collect(Collectors.toList());

    return filterResults(selected, selector.filter(), selector.trim(), selector.multi());
  }

  @Override
  protected Document parse(String content) {
    return Jsoup.parse(content);
  }
}
