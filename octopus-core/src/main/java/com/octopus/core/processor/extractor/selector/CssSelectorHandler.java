package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
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
public class CssSelectorHandler extends CacheableSelectorHandler<Document, CssSelector> {

  @Override
  public List<String> selectWithType(Document document, CssSelector selector, Response response) {
    Elements elements = document.select(selector.expression());
    List<String> selected =
        elements.stream()
            .map(
                e -> {
                  if (StrUtil.isNotBlank(selector.attr())) {
                    return e.attr(selector.attr());
                  } else if (selector.self()) {
                    return e.toString();
                  } else {
                    return e.text();
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
