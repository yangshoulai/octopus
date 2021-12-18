package com.octopus.core.processor.matcher;

import com.octopus.core.Response;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/18
 */
public class NotMatcher implements Matcher {

  private final Matcher matcher;

  public NotMatcher(@NonNull Matcher matcher) {
    this.matcher = matcher;
  }

  @Override
  public boolean matches(Response response) {
    return !matcher.matches(response);
  }
}
