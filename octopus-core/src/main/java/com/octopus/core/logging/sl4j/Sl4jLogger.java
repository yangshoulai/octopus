package com.octopus.core.logging.sl4j;

import cn.hutool.core.util.ClassLoaderUtil;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.logging.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/30
 */
public class Sl4jLogger implements Logger {

  private final org.slf4j.Logger logger;

  private static final String BINDER_CLASS = "org.slf4j.impl.StaticLoggerBinder";

  public Sl4jLogger(String name) {
    if (ClassLoaderUtil.isPresent(BINDER_CLASS)) {
      this.logger = LoggerFactory.getLogger(name);
      if (this.logger instanceof NOPLogger) {
        throw new OctopusException("Sl4j implementation not found");
      }
    } else {
      throw new OctopusException("Class " + BINDER_CLASS + " not found");
    }
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public void error(String s, Throwable e) {
    logger.error(s, e);
  }

  @Override
  public void error(String s) {
    logger.error(s);
  }

  @Override
  public void debug(String s) {
    logger.debug(s);
  }

  @Override
  public void trace(String s) {
    logger.trace(s);
  }

  @Override
  public void warn(String s) {
    logger.warn(s);
  }

  @Override
  public void info(String s) {
    logger.info(s);
  }
}
