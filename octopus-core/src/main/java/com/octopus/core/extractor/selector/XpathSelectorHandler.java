package com.octopus.core.extractor.selector;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
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
public class XpathSelectorHandler extends CacheableSelector<Node, XpathSelector> {

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
    try {
      NodeList nodes = XmlUtil.getNodeListByXPath(selector.expression(), document);
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        String value = null;
        if (node instanceof CharacterData) {
          value = ((CharacterData) node).getData();
        } else if (node instanceof Attr) {
          value = ((Attr) node).getValue();
        } else {
          value = XmlUtil.unescape(XmlUtil.toStr(node));
        }
        results.add(value);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return filterResults(results, selector.filter(), selector.trim(), selector.multi());
  }

  @Override
  protected Node parse(String content) {
    try {
      if (StrUtil.trim(content).startsWith("<?xml")) {
        return XmlUtil.parseXml(content);
      } else {
        try {
          return this.serializer.createDOM(this.cleaner.clean(content));
        } catch (ParserConfigurationException e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
