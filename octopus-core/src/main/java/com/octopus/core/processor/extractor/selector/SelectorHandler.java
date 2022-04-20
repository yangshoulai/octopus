package com.octopus.core.processor.extractor.selector;

import com.octopus.core.Response;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public interface SelectorHandler<S extends Annotation> {

  /**
   * 提取内容
   *
   * @param content 原文
   * @param response 原始响应
   * @return 提取的内容
   */
  List<String> select(String content, S selector, Response response) throws Exception;
}
