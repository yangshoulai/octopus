package com.octopus.core.processor;

import com.octopus.core.Request;
import com.octopus.core.Response;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public interface Processor {

  /**
   * 解析响应并返回新的下载请求
   *
   * @param response 下载响应
   * @return 新的下载请求
   */
  List<Request> process(Response response);

  /**
   * 判断当前处理器是否需要处理指定下载响应
   *
   * @param response 下载响应
   * @return 是否需要处理
   */
  boolean matches(Response response);
}
