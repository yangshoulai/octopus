package com.octopus.core.processor;

import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.logging.Logger;
import com.octopus.core.logging.LoggerFactory;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class LoggerProcessor implements Processor {

  private final Logger log = LoggerFactory.getLogger("Octopus");

  @Override
  public List<Request> process(Response response) {
    log.info(response.toString());
    return null;
  }

  @Override
  public boolean matches(Response response) {
    return true;
  }
}
