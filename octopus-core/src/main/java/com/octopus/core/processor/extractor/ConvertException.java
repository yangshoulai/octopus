package com.octopus.core.processor.extractor;

import com.octopus.core.exception.OctopusException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/4/21
 */
public class ConvertException extends OctopusException {

  public ConvertException(String val, Class<?> targetClass) {
    super("Can not convert string " + val + " to type " + targetClass);
  }
}
