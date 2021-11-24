package com.octopus.core.processor.handler;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public interface TypeHandler<T> {

  T handle(String val);
}
