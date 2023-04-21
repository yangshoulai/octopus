package com.octopus.core.store;

import com.octopus.core.Request;
import com.octopus.core.Request.State;
import com.octopus.core.Request.Status;
import com.octopus.core.replay.ReplayFilter;
import java.util.List;
import java.util.Map;
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
  }

  @Override
  public void markAsCompleted(Request request) {
    this.all.get(request.getId()).setStatus(Status.of(State.Completed));
  }

  @Override
  public void markAsFailed(Request request, String error) {
    this.all.get(request.getId()).setStatus(Status.of(State.Failed, error));
  }

  @Override
  public long getTotalSize() {
    return this.all.size();
  }

  @Override
  public long getCompletedSize() {
    return this.all.values().stream()
        .filter(r -> r.getStatus().getState() == State.Completed)
        .count();
  }

  @Override
  public long getWaitingSize() {
    return this.waiting.size();
  }

  @Override
  public long getFailedSize() {
    return this.all.values().stream().filter(r -> r.getStatus().getState() == State.Failed).count();
  }

  @Override
  public List<Request> getFailed() {
    return this.all.values().stream()
        .filter(r -> r.getStatus().getState() == State.Failed)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(String id) {
    this.all.remove(id);
    this.waiting.removeIf(r -> r.getId().equals(id));
  }

  @Override
  public int replayFailed(ReplayFilter filter) {
    List<Request> failed =
        this.all.values().stream()
            .filter(r -> r.getStatus().getState() == State.Failed)
            .filter(filter::filter)
            .collect(Collectors.toList());
    failed.forEach(r -> r.setFailTimes(r.getFailTimes() + 1));
    failed.forEach(r -> r.setStatus(Status.of(State.Waiting)));
    failed.forEach(this::put);
    return failed.size();
  }
}
