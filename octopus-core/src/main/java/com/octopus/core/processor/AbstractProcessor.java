package com.octopus.core.processor;

import com.octopus.core.Processor;
import com.octopus.core.Response;
import com.octopus.core.processor.matcher.Matcher;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public abstract class AbstractProcessor implements Processor {

  private final Matcher matcher;

  public AbstractProcessor(@NonNull Matcher matcher) {
    this.matcher = matcher;
  }

  @Override
  public boolean matches(Response response) {
    return matcher.matches(response);
  }
}
