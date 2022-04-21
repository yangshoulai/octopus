package com.octopus.core.processor.extractor;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/27
 */
public interface Collector<R> {

  /**
   * 提取结果收集
   *
   * @param r 结果
   */
  void collect(R r);
}
