package com.octopus.core.exception;

import com.octopus.core.Response;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class BadStatusException extends DownloadException {

  private Response response;

  public BadStatusException(Response response) {
    super(String.format("Bad status [%s]", response.getStatus()));
    this.response = response;
  }

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }
}
