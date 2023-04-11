package com.octopus.core.store;

import cn.hutool.core.collection.ListUtil;
import com.octopus.core.Request;
import com.octopus.core.Request.State;
import com.octopus.core.Request.Status;
import com.octopus.core.replay.ReplayFilter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class MemoryStore implements Store {

  private final Map<String, Request> all = new ConcurrentHashMap<>();

  private final BlockingQueue<Request> waiting = new PriorityBlockingQueue<>();

  private final List<Request> failed = new CopyOnWriteArrayList<>();

  private final AtomicInteger completedCounter = new AtomicInteger();

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
    this.failed.clear();
    this.completedCounter.set(0);
  }

  @Override
  public void markAsCompleted(Request request) {
    this.all.get(request.getId()).setStatus(Status.of(State.Completed));
    this.completedCounter.incrementAndGet();
  }

  @Override
  public void markAsFailed(Request request, String error) {
    this.all.get(request.getId()).setStatus(Status.of(State.Failed, error));
    this.failed.add(request);
  }

  @Override
  public long getTotalSize() {
    return this.all.size();
  }

  @Override
  public long getCompletedSize() {
    return this.completedCounter.get();
  }

  @Override
  public long getWaitingSize() {
    return this.waiting.size();
  }

  @Override
  public long getFailedSize() {
    return this.failed.size();
  }

  @Override
  public List<Request> getFailed() {
    return ListUtil.unmodifiable(this.failed);
  }

  @Override
  public void delete(String id) {
    this.all.remove(id);
    this.waiting.removeIf(r -> r.getId().equals(id));
    this.failed.removeIf(r -> r.getId().equals(id));
  }

  @Override
  public int replayFailed(ReplayFilter filter) {
    List<Request> requests =
        this.failed.stream().filter(filter::filter).collect(Collectors.toList());
    this.failed.removeIf(filter::filter);
    requests.forEach(r -> r.setFailTimes(r.getFailTimes() + 1));
    requests.forEach(r -> r.setStatus(Status.of(State.Waiting)));
    requests.forEach(this::put);
    return requests.size();
  }
}
