package com.octopus.core.logging;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public interface Logger {

  boolean isDebugEnabled();

  boolean isTraceEnabled();

  void error(String s, Throwable e);

  void error(String s);

  void debug(String s);

  void trace(String s);

  void warn(String s);

  void info(String s);
}
