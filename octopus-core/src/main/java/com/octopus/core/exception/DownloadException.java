package com.octopus.core.exception;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class DownloadException extends OctopusException {

  public DownloadException(String message) {
    super(message);
  }

  public DownloadException(Throwable cause) {
    super(cause);
  }

  public DownloadException(String message, Throwable cause) {
    super(message, cause);
  }
}
