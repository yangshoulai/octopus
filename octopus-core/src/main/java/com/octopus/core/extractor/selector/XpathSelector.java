package com.octopus.core.extractor.selector;

import cn.hutool.core.util.XmlUtil;
import com.octopus.core.extractor.annotation.Selector;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
public class XpathSelector extends CacheableSelector<Node> {

  private HtmlCleaner cleaner;

  private DomSerializer serializer;

  private DocumentBuilder builder;

  public XpathSelector() {
    CleanerProperties properties = new CleanerProperties();
    this.cleaner = new HtmlCleaner(properties);
    this.serializer = new DomSerializer(new CleanerProperties());

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      dbf.setValidating(false);
      dbf.setNamespaceAware(false);
      dbf.setFeature("http://xml.org/sax/features/namespaces", false);
      dbf.setFeature("http://xml.org/sax/features/validation", false);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      this.builder = dbf.newDocumentBuilder();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<String> selectWithType(Node document, Selector selector) {
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
    return results;
  }

  @Override
  protected Node parse(String content) {
    try {
      return this.builder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      try {
        return this.serializer.createDOM(this.cleaner.clean(content));
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
    return null;
  }
}
