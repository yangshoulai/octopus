package com.octopus.core.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class XpathSelector implements ISelector {

  private String selector;

  private String attr;

  private boolean multi = true;

  private boolean filter = true;

  public XpathSelector(@NonNull String selector) {
    this.selector = selector;
  }

  public String getSelector() {
    return selector;
  }

  public void setSelector(String selector) {
    this.selector = selector;
  }

  public boolean isMulti() {
    return multi;
  }

  public void setMulti(boolean multi) {
    this.multi = multi;
  }

  public boolean isFilter() {
    return filter;
  }

  public void setFilter(boolean filter) {
    this.filter = filter;
  }

  public void setAttr(String attr) {
    this.attr = attr;
  }

  public String getAttr() {
    return attr;
  }

  @Override
  public List<String> select(String content) {
    List<String> results = new ArrayList<>();
    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode node = cleaner.clean(content);
    try {
      Object[] objs = node.evaluateXPath(this.selector);
      if (objs != null) {
        for (Object obj : objs) {
          String value = null;
          if (obj != null) {
            if (obj instanceof CharSequence) {
              value = obj.toString();
            } else if (obj instanceof TagNode) {
              TagNode tag = (TagNode) obj;
              if (StrUtil.isNotBlank(attr)) {
                value = tag.getAttributeByName(attr);
              } else {
                value = tag.getText().toString();
              }
            }
          }
          results.add(value);
        }
      }
    } catch (XPatherException e) {
      e.printStackTrace();
    }

    if (this.filter) {
      results = results.stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
    }
    if (this.multi) {
      return results;
    }
    String result = results.stream().filter(StrUtil::isNotBlank).findFirst().orElse(null);
    return result == null ? new ArrayList<>() : ListUtil.toList(result);
  }
}
