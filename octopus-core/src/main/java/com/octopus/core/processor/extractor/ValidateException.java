package com.octopus.core.processor.extractor;

import com.octopus.core.exception.OctopusException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class ValidateException extends OctopusException {

  public ValidateException(String message) {
    super(message);
  }

  public ValidateException(Throwable cause) {
    super(cause);
  }

  public ValidateException(String message, Throwable cause) {
    super(message, cause);
  }
}
