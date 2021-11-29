package com.octopus.core.extractor.selector;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import com.octopus.core.extractor.annotation.Selector;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.w3c.dom.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class XpathSelector extends CacheableSelector<Node> {

  private HtmlCleaner cleaner;

  private DomSerializer serializer;

  public XpathSelector() {
    CleanerProperties properties = new CleanerProperties();
    this.cleaner = new HtmlCleaner(properties);
    this.serializer = new DomSerializer(new CleanerProperties(), false);
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
    if (StrUtil.trim(content).startsWith("<?xml")) {
      return XmlUtil.parseXml(content);
    } else {
      try {
        return this.serializer.createDOM(this.cleaner.clean(content));
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
