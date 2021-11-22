package com.octopus.core.processor.matcher;

import com.octopus.core.Response;
import java.util.List;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class AndMatcher implements Matcher {
  private final List<Matcher> matchers;

  public AndMatcher(@NonNull List<Matcher> matchers) {
    this.matchers = matchers;
  }

  @Override
  public boolean matches(Response response) {
    return this.matchers.stream().allMatch(matcher -> matcher.matches(response));
  }
}
