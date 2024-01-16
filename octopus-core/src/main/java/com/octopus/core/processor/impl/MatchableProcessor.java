package com.octopus.core.processor.impl;

import com.octopus.core.Response;
import com.octopus.core.processor.Processor;
import com.octopus.core.processor.matcher.Matcher;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public abstract class MatchableProcessor implements Processor {

  private final Matcher matcher;

  public MatchableProcessor(@NonNull Matcher matcher) {
    this.matcher = matcher;
  }

  public boolean matches(Response response) {
    return matcher.matches(response);
  }
}
