package com.octopus.core.processor.handler;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class StringHandler implements TypeHandler<String> {

  @Override
  public String handle(String val) {
    return val == null ? null : val.trim();
  }
}
