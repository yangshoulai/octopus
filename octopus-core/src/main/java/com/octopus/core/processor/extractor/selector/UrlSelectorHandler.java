package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class UrlSelectorHandler extends AbstractSelectorHandler {

  @Override
  public List<String> doMultiSelect(String content, Selector selector, Response response)
      throws SelectException {
    return ListUtil.of(response.getRequest().getUrl());
  }
}
