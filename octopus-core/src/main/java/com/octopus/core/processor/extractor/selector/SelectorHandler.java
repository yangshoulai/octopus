package com.octopus.core.processor.extractor.selector;

import com.octopus.core.Response;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public interface SelectorHandler {

  /**
   * 提取内容
   *
   * @param source 原文
   * @param selector 选择器
   * @param multi 是否多选
   * @param response 原始响应
   * @return 提取的内容
   */
  List<String> select(String source, Selector selector, boolean multi, Response response)
      throws SelectException;
}
