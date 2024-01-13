package com.octopus.core.exception;

import com.octopus.core.exception.OctopusException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class SelectorNotFoundException extends OctopusException {

  public SelectorNotFoundException(String message) {
    super(message);
  }

  public SelectorNotFoundException(Throwable cause) {
    super(cause);
  }

  public SelectorNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
