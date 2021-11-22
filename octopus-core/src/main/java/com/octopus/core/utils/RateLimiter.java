package com.octopus.core.utils;

import cn.hutool.core.thread.NamedThreadFactory;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class RateLimiter {

  /** 最大数量 */
  private final int max;

  /** 时间间隔 */
  private final int period;

  /** 时间间隔单位 */
  private final TimeUnit unit;

  private final Semaphore semaphore;

  private ScheduledExecutorService scheduler;

  public RateLimiter(int max, int period, TimeUnit unit) {
    if (max <= 0) {
      throw new IllegalArgumentException("max must > 0");
    }
    this.max = max;
    this.period = period;
    this.unit = unit;
    this.semaphore = new Semaphore(this.max);
  }

  public void start() {
    if (this.scheduler == null || this.scheduler.isShutdown()) {
      this.scheduler = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("worker-", false));
    }
    this.scheduler.scheduleAtFixedRate(
        () -> this.semaphore.release(this.max - this.semaphore.availablePermits()),
        0,
        this.period,
        this.unit);
  }

  public void stop() {
    if (this.scheduler != null && !this.scheduler.isShutdown()) {
      this.scheduler.shutdown();
    }
  }

  public void acquire() throws InterruptedException {
    if (this.scheduler != null && !this.scheduler.isShutdown()) {
      this.semaphore.acquire();
    }
    throw new IllegalStateException("Rate limiter must start before acquire a permit");
  }

  public static RateLimiter of(int max) {
    return of(max, 1);
  }

  public static RateLimiter of(int max, int periodSeconds) {
    return of(max, periodSeconds, TimeUnit.SECONDS);
  }

  public static RateLimiter of(int max, int period, TimeUnit unit) {
    return new RateLimiter(max, period, unit);
  }
}
