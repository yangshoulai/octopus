package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import com.octopus.core.Response;
import java.util.List;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Data
public class ParamSelectorHandler implements SelectorHandler<ParamSelector> {

  @Override
  public List<String> select(String content, ParamSelector selector, Response response)
      throws Exception {
    String result = response.getRequest().getParams().get(selector.name());
    if (result == null) {
      CharSequence paramValue =
          UrlBuilder.of(response.getRequest().getUrl()).getQuery().get(selector.name());
      if (paramValue != null) {
        result = paramValue.toString();
      }
    }
    if (result == null) {
      result = selector.def();
    }
    return result == null ? ListUtil.empty() : ListUtil.of(result);
  }
}
