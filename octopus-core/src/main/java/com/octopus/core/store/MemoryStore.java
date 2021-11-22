package com.octopus.core.store;

import com.octopus.core.Request;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class MemoryStore implements Store {

  private final LinkedBlockingQueue<Request> requests = new LinkedBlockingQueue<>();

  @Override
  public Request get() {
    return this.requests.poll();
  }

  @Override
  public boolean put(Request request) {
    return requests.offer(request);
  }
}
