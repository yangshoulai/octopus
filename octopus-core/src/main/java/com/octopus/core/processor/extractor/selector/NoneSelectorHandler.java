package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Response;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/4
 */
public class NoneSelectorHandler extends AbstractSelectorHandler {

  @Override
  protected List<String> doMultiSelect(String source, Selector selector, Response response)
      throws SelectException {
    return ListUtil.empty();
  }

  @Override
  protected String doSingleSelect(String source, Selector selector, Response response)
      throws SelectException {
    return null;
  }
}
