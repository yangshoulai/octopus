package com.octopus.core.store;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.octopus.core.Request;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class MemoryStore implements Store {

  private final Set<String> idSet = new ConcurrentHashSet<>();

  private final BlockingQueue<Request> requests = new PriorityBlockingQueue<>();

  private final AtomicLong completed = new AtomicLong(0);

  private final AtomicLong total = new AtomicLong(0L);

  private final AtomicLong failed = new AtomicLong(0);

  @Override
  public Request get() {
    return this.requests.poll();
  }

  @Override
  public boolean put(Request request) {
    boolean success = requests.offer(request);
    if (success) {
      idSet.add(request.getId());
      this.total.incrementAndGet();
    }
    return success;
  }

  @Override
  public boolean exists(Request request) {
    return idSet.contains(request.getId());
  }

  @Override
  public void clear() {
    this.idSet.clear();
    this.requests.clear();
    this.completed.set(0);
    this.total.set(0);
    this.failed.set(0);
  }

  @Override
  public void markAsCompleted(Request request) {
    this.completed.incrementAndGet();
  }

  @Override
  public void markAsFailed(Request request) {
    this.failed.incrementAndGet();
  }

  @Override
  public long getTotalSize() {
    return this.total.get();
  }

  @Override
  public long getCompletedSize() {
    return this.completed.get();
  }

  @Override
  public long getWaitingSize() {
    return this.requests.size();
  }
}
