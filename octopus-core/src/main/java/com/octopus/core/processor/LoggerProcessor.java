package com.octopus.core.processor;

import com.octopus.core.Octopus;
import com.octopus.core.Response;
import com.octopus.core.logging.Logger;
import com.octopus.core.logging.LoggerFactory;
import com.octopus.core.processor.matcher.Matcher;
import com.octopus.core.processor.matcher.Matchers;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class LoggerProcessor extends MatchableProcessor {

  private final Logger log = LoggerFactory.getLogger("Octopus");

  public LoggerProcessor() {
    this(Matchers.ALL);
  }

  public LoggerProcessor(@NonNull Matcher matcher) {
    super(matcher);
  }

  @Override
  public void process(Response response, Octopus octopus) {
    log.info(response.toString());
  }
}
