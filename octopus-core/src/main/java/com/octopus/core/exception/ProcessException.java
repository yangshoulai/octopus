package com.octopus.core.exception;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class ProcessException extends OctopusException {

  public ProcessException(Throwable cause) {
    super(cause);
  }

  public ProcessException(String message, Throwable cause) {
    super(message, cause);
  }
}
