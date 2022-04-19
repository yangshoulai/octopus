package com.octopus.core.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import java.util.List;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Data
public class UrlSelectorHandler implements SelectorHandler<UrlSelector> {

  @Override
  public List<String> select(String content, UrlSelector selector, Response response)
      throws Exception {
    return ListUtil.of(response.getRequest().getUrl());
  }
}
