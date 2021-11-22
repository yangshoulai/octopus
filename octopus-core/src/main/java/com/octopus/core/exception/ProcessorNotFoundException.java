package com.octopus.core.exception;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class ProcessorNotFoundException extends OctopusException {

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
