package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import java.util.List;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Data
public class AttrSelectorHandler implements SelectorHandler<AttrSelector> {

  @Override
  public List<String> select(String content, AttrSelector selector, Response response)
      throws Exception {
    Object attr = response.getRequest().getAttribute(selector.name());
    String val = null;
    if (attr != null) {
      val = attr.toString();
    }
    if (val == null) {
      val = selector.def();
    }
    return val == null ? ListUtil.empty() : ListUtil.of(val);
  }
}
