package com.octopus.core.processor.extractor.selector;

import com.octopus.core.exception.OctopusException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/27
 */
public class SelectException extends OctopusException {

  public SelectException(String message) {
    super(message);
  }

  public SelectException(String message, Throwable cause) {
    super(message, cause);
  }

  public SelectException(Throwable cause) {
    super(cause);
  }
}
