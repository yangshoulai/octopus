package com.octopus.core.store;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.octopus.core.Request;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class MemoryStore implements Store {

  private final Map<String, Request> all = new ConcurrentHashMap<>();

  private final BlockingQueue<Request> waiting = new PriorityBlockingQueue<>();

  private final Set<String> completed = new ConcurrentHashSet<>();

  private final Set<String> failed = new ConcurrentHashSet<>();

  @Override
  public Request get() {
    return this.waiting.poll();
  }

  @Override
  public boolean put(Request request) {
    boolean success = waiting.offer(request);
    if (success) {
      all.put(request.getId(), request);
    }
    return success;
  }

  @Override
  public boolean exists(Request request) {
    return all.containsKey(request.getId());
  }

  @Override
  public void clear() {
    this.all.clear();
    this.waiting.clear();
    this.completed.clear();
    this.failed.clear();
  }

  @Override
  public void markAsCompleted(Request request) {
    this.completed.add(request.getId());
  }

  @Override
  public void markAsFailed(Request request) {
    this.failed.add(request.getId());
  }

  @Override
  public long getTotalSize() {
    return this.all.size();
  }

  @Override
  public long getCompletedSize() {
    return this.completed.size();
  }

  @Override
  public long getWaitingSize() {
    return this.waiting.size();
  }

  @Override
  public List<Request> getFailed() {
    return this.failed.stream().map(all::get).collect(Collectors.toList());
  }
}
