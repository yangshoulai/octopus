package com.octopus.core.extractor.selector;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import com.octopus.core.extractor.annotation.Selector;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
public class CssSelector extends CacheableSelector<Document> {

  @Override
  public List<String> selectWithType(Document document, Selector selector) {

    Elements elements = document.select(selector.expression());
    Stream<String> stream =
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
            .filter(s -> !selector.filter() || StrUtil.isNotBlank(s));
    return stream.collect(Collectors.toList());
  }

  @Override
  protected Document parse(String content) {
    return Jsoup.parse(content);
  }
}
