package com.octopus.core.logging;

import com.octopus.core.logging.stdout.StdOutLogger;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class LoggerFactory {

  public static Logger getLogger(String logger) {
    return new StdOutLogger();
  }

  public static Logger getLogger(Class<?> cls) {
    return getLogger(cls.getName());
  }
}
