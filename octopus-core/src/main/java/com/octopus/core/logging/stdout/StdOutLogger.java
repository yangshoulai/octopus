package com.octopus.core.logging.stdout;

import com.octopus.core.logging.Logger;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class StdOutLogger implements Logger {

  public StdOutLogger(String name) {}

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public boolean isTraceEnabled() {
    return true;
  }

  @Override
  public void error(String s, Throwable e) {
    System.err.println(s);
    e.printStackTrace(System.err);
  }

  @Override
  public void error(String s) {
    System.err.println(s);
  }

  @Override
  public void debug(String s) {
    System.out.println(s);
  }

  @Override
  public void trace(String s) {
    System.out.println(s);
  }

  @Override
  public void warn(String s) {
    System.out.println(s);
  }

  @Override
  public void info(String s) {
    System.out.println(s);
  }
}
