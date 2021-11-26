package com.octopus.core.extractor.selector;

import cn.hutool.core.util.XmlUtil;
import com.octopus.core.extractor.annotation.Selector;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class XpathSelector extends CacheableSelector<Document> {

  private HtmlCleaner cleaner;

  private DomSerializer serializer;

  public XpathSelector() {
    CleanerProperties properties = new CleanerProperties();
    properties.setAdvancedXmlEscape(false);
    this.cleaner = new HtmlCleaner(properties);
    this.serializer = new DomSerializer(properties, false);
  }

  @Override
  public List<String> select(Document document, Selector selector) {
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
          value = XmlUtil.toStr(node);
        }
        results.add(value);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  @Override
  protected Document parse(String content) {
    try {
      return this.serializer.createDOM(cleaner.clean(content));
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    return null;
  }
}
