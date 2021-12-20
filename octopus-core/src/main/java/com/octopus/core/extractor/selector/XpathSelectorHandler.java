package com.octopus.core.extractor.selector;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class XpathSelectorHandler extends CacheableSelectorHandler<Node, XpathSelector> {

  private HtmlCleaner cleaner;

  private DomSerializer serializer;

  public XpathSelectorHandler() {
    CleanerProperties properties = new CleanerProperties();
    this.cleaner = new HtmlCleaner(properties);
    this.serializer = new DomSerializer(new CleanerProperties(), false);
  }

  @Override
  public List<String> selectWithType(Node document, XpathSelector selector) {
    List<String> results = new ArrayList<>();

    if (selector.node()) {
      NodeList nodes = XmlUtil.getNodeListByXPath(selector.expression(), document);
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        String value = null;
        if (node instanceof CharacterData) {
          value = ((CharacterData) node).getData();
        } else if (node instanceof Attr) {
          value = ((Attr) node).getValue();
        } else {
          value = XmlUtil.toStr(node, CharsetUtil.UTF_8, false, true);
        }
        if (!StrUtil.isBlank(value)) {
          value = EscapeUtil.unescapeXml(value);
        }
        results.add(value);
      }
    } else {
      Object val = XmlUtil.getByXPath(selector.expression(), document, XPathConstants.STRING);
      if (val != null) {
        results.add(val.toString());
      }
    }

    return filterResults(results, selector.filter(), selector.trim(), selector.multi());
  }

  @Override
  protected Node parse(String content) throws Exception {

    return this.serializer.createDOM(this.cleaner.clean(content));
  }
}
