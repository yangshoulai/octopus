package com.octopus.core.replay;

import com.octopus.core.Request;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/1/11
 */
public class HttpStatusReplayFilter implements ReplayFilter {

  private final int status;

  public HttpStatusReplayFilter(int status) {
    this.status = status;
  }

  @Override
  public boolean filter(Request request) {
    return request.getStatus() != null
        && request.getStatus().getMessage() != null
        && request.getStatus().getMessage().contains("Bad status [" + status + "] for request");
  }
}
