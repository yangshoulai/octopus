package com.octopus.core.extractor;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/27
 */
public interface Collector<R> {

  void collect(R r);
}
