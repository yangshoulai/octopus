package com.octopus.core.utils;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/23
 */
public class AvgRateLimiter {

    private final long period;

    private final AtomicLong lastAcquiredTime = new AtomicLong(System.currentTimeMillis());

    public AvgRateLimiter(double maxPerSecond) {
        this.period = (long) (1000d / maxPerSecond);
    }

    public static AvgRateLimiter of(double maxPerSecond) {
        if (maxPerSecond <= 0) {
            throw new IllegalArgumentException("maxPerSecond must > 0");
        }
        return new AvgRateLimiter(maxPerSecond);
    }

    public static AvgRateLimiter of(int max, int period, TimeUnit unit) {
        return new AvgRateLimiter(max * 1.0d / unit.toSeconds(period));
    }

    public static AvgRateLimiter of(int max, int periodSeconds) {
        return new AvgRateLimiter(max * 1.0d / periodSeconds);
    }

    public static void main(String[] args) {
        AvgRateLimiter limiter = new AvgRateLimiter(20);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    limiter.acquire();
                    System.out.println("Thread " + Thread.currentThread().getName() + " acquired at " + DateUtil.format(new Date(), DatePattern.NORM_DATETIME_MS_PATTERN));
                }
            }
        };

        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(r);
            t.setName(String.valueOf(i));
            t.start();
        }
    }

    public void acquire() {
        while (true) {
            long now = System.currentTimeMillis();
            long last = lastAcquiredTime.get();
            long p = now - last;
            if (p < period) {
                ThreadUtil.safeSleep(period - p);
            } else if (lastAcquiredTime.compareAndSet(last, now)) {
                break;
            }
        }
    }
}
