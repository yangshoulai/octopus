package com.octopus.core.exception;

import com.octopus.core.Request;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class ProcessorNotFoundException extends OctopusException {

  public ProcessorNotFoundException(Request request) {
    super(String.format("No processor found for request [%s]", request.toString()));
  }

  public ProcessorNotFoundException(String message) {
    super(message);
  }

  public ProcessorNotFoundException(Throwable cause) {
    super(cause);
  }

  public ProcessorNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
