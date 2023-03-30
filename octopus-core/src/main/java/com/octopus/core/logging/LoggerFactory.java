package com.octopus.core.logging;

import com.octopus.core.exception.OctopusException;
import com.octopus.core.logging.sl4j.Sl4jLogger;
import com.octopus.core.logging.stdout.StdOutLogger;
import java.lang.reflect.Constructor;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class LoggerFactory {

  private static Constructor<? extends Logger> constructor;

  static {
    tryUsingLogger(Sl4jLogger.class);
    tryUsingLogger(StdOutLogger.class);
  }

  private static void tryUsingLogger(Class<? extends Logger> logger) {
    if (constructor == null) {
      try {
        Constructor<? extends Logger> c = logger.getConstructor(String.class);
        Logger l = c.newInstance(LoggerFactory.class.getName());
        if (l.isDebugEnabled()) {
          l.debug("Using log implementation " + logger);
        }
        constructor = c;
      } catch (Throwable e) {
        // ignore
      }
    }
  }

  public static Logger getLogger(String name) {
    try {
      return constructor.newInstance(name);
    } catch (Throwable e) {
      throw new OctopusException("Error creating logger for logger " + name, e);
    }
  }

  public static Logger getLogger(Class<?> cls) {
    return getLogger(cls.getName());
  }
}
