package com.octopus.core.processor.impl;

import com.octopus.core.Octopus;
import com.octopus.core.Response;
import com.octopus.core.processor.Processor;
import com.octopus.core.processor.matcher.Matcher;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/30
 */
public final class MatchedProcessor extends MatchableProcessor {

  private final Processor delegate;

  public MatchedProcessor(@NonNull Matcher matcher, @NonNull Processor processor) {
    super(matcher);
    this.delegate = processor;
  }

  @Override
  public void process(Response response, Octopus octopus) {
    this.delegate.process(response, octopus);
  }
}
