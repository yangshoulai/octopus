package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import com.octopus.core.Response;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class ParamSelectorHandler extends AbstractSelectorHandler {

  @Override
  public List<String> doMultiSelect(String source, Selector selector, Response response) {
    String result = response.getRequest().getParams().get(selector.value());
    if (result == null) {
      CharSequence paramValue =
          UrlBuilder.of(response.getRequest().getUrl()).getQuery().get(selector.value());
      if (paramValue != null) {
        result = paramValue.toString();
      }
    }
    return result == null ? null : ListUtil.of(result);
  }
}
