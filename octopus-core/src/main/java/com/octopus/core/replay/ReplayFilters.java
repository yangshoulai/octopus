package com.octopus.core.replay;

import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/1/11
 */
public final class ReplayFilters {

  public static ReplayFilter status(int status) {
    return new HttpStatusReplayFilter(status);
  }

  public static ReplayFilter and(@NonNull ReplayFilter... filters) {
    return (request -> {
      if (filters == null) {
        return false;
      }
      for (ReplayFilter replayFilter : filters) {
        if (!replayFilter.filter(request)) {
          return false;
        }
      }
      return true;
    });
  }

  public static ReplayFilter or(@NonNull ReplayFilter... filters) {
    return (request -> {
      if (filters == null) {
        return false;
      }
      for (ReplayFilter replayFilter : filters) {
        if (replayFilter.filter(request)) {
          return true;
        }
      }
      return false;
    });
  }

  public static ReplayFilter not(@NonNull ReplayFilter filter) {
    return (request -> !filter.filter(request));
  }

  public static ReplayFilter all() {
    return ((request) -> true);
  }
}
