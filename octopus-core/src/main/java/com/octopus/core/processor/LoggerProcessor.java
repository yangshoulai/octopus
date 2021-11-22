package com.octopus.core.processor;

import com.octopus.core.Processor;
import com.octopus.core.Request;
import com.octopus.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class LoggerProcessor implements Processor {

  private final Logger log = LoggerFactory.getLogger("Octopus");

  @Override
  public List<Request> process(Response response) {
    log.info("{}", response);
    return null;
  }

  @Override
  public boolean matches(Response response) {
    return true;
  }
}
