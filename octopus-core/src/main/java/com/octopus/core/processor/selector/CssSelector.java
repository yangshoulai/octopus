package com.octopus.core.processor.selector;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.properties.selector.CssSelectorProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CssSelector extends AbstractCacheableSelector<Document, CssSelectorProperties> {
    @Override
    public List<String> doSelectWithDoc(
            Document document, CssSelectorProperties selector, boolean multi, Response response) {
        Elements elements = new Elements();
        if (multi) {
            elements = document.select(selector.getExpression());
        } else {
            Element e = document.selectFirst(selector.getExpression());
            if (e != null) {
                elements = new Elements(e);
            }
        }
        return elements.stream()
                .map(
                        e -> {
                            if (StrUtil.isNotBlank(selector.getAttr())) {
                                return e.attr(selector.getAttr());
                            } else if (selector.isSelf()) {
                                return e.toString();
                            } else {
                                return e.text();
                            }
                        })
                .collect(Collectors.toList());
    }

    @Override
    protected Document parse(String content) {
        return Jsoup.parse(content);
    }


}
