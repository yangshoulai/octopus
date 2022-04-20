package com.octopus.core.processor.extractor;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/27
 */
public interface Collector<R> {

  void collect(R r);
}
