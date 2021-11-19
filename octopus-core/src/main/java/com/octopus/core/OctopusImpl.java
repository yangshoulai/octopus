package com.octopus.core;

import cn.hutool.core.thread.NamedThreadFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
@Slf4j
class OctopusImpl implements Octopus {

  private final AtomicReference<OctopusState> state = new AtomicReference<>(OctopusState.NEW);

  private ExecutorService boss;

  private ExecutorService workers;

  private int threads;

  private Semaphore workerSemaphore;

  private final Lock lock = new ReentrantLock();

  private final Condition idleCondition = lock.newCondition();

  private OctopusDownloader downloader;

  private OctopusStore store;

  @Override
  public void start() throws OctopusException {
    Future<Void> future = this.startAsync();
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new OctopusException(e);
    }
  }

  @Override
  public Future<Void> startAsync() throws OctopusException {
    if (!this.state.compareAndSet(OctopusState.NEW, OctopusState.STARTING)) {
      throw new OctopusException("Illegal octopus state [" + this.state.get().getLabel() + "]");
    }
    log.debug(
        "Octopus state [{}] => [{}]",
        OctopusState.NEW.getLabel(),
        OctopusState.STARTING.getLabel());
    log.info("Octopus starting");
    this.boss = this.createBossExecutor();
    this.workers = this.createWorkerExecutor();
    this.workerSemaphore = new Semaphore(this.threads);
    CompletableFuture<Void> future = CompletableFuture.runAsync(this::dispatch, this.boss);
    this.state.set(OctopusState.STARTED);
    log.debug(
        "Octopus state [{}] => [{}]",
        OctopusState.STARTING.getLabel(),
        OctopusState.STARTED.getLabel());
    log.info("Octopus started");
    return future;
  }

  @Override
  public void stop() throws OctopusException {
    OctopusState state = this.state.get();
    if (state != OctopusState.STARTED
        && state != OctopusState.IDLE
        && this.state.compareAndSet(state, OctopusState.STOPPING)) {
      throw new OctopusException("Illegal octopus state [" + this.state.get().getLabel() + "]");
    }
    log.debug("Octopus state [{}] => [{}]", state.getLabel(), OctopusState.STARTED.getLabel());
    log.info("Octopus stopping");
    this.boss.shutdown();
    this.workers.shutdown();
    this.state.set(OctopusState.STOPPED);
    log.info("Octopus stopped");
  }

  @Override
  public void addRequest(OctopusRequest request) throws OctopusException {
    OctopusState state = this.state.get();
    if (state.getState() >= OctopusState.STOPPING.getState()) {
      throw new OctopusException("Illegal octopus state [" + this.state.get().getLabel() + "]");
    }
    this.store.put(request);
    if (this.state.compareAndSet(OctopusState.IDLE, OctopusState.STARTED)) {
      log.debug(
          "Octopus state [{}] => [{}]",
          OctopusState.IDLE.getLabel(),
          OctopusState.STARTED.getLabel());
      lock.lock();
      try {
        this.idleCondition.signalAll();
      } finally {
        lock.unlock();
      }
    }
  }

  private void dispatch() {
    OctopusState state;
    while ((state = this.state.get()) == OctopusState.STARTED || state == OctopusState.IDLE) {
      try {
        // dispatch download request
        OctopusRequest request = this.store.get();
        if (request != null) {
          this.workers.execute(
              () -> {
                try {
                  this.workerSemaphore.acquire();
                  OctopusResponse response = this.downloader.download(request);
                  this.process(response);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                } finally {
                  this.workerSemaphore.release();
                }
              });
        } else {
          if (this.workerSemaphore.availablePermits() == this.threads) {
            log.info("No more requests found, octopus will idle");
            if (this.state.compareAndSet(OctopusState.STARTED, OctopusState.IDLE)) {
              log.debug(
                  "Octopus state [{}] => [{}]",
                  OctopusState.STARTED.getLabel(),
                  OctopusState.IDLE.getLabel());
              lock.lock();
              try {
                idleCondition.await();
              } finally {
                lock.unlock();
              }
            }
          }
        }
      } catch (Throwable e) {
        log.error("Error when dispatch request", e);
      }
    }
  }

  private void process(OctopusResponse response) {}

  private ExecutorService createBossExecutor() {
    return new ThreadPoolExecutor(
        1,
        1,
        0,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        r -> {
          Thread t = new Thread(r);
          t.setName("boss");
          return t;
        });
  }

  private ExecutorService createWorkerExecutor() {
    return new ThreadPoolExecutor(
        this.threads,
        this.threads,
        0,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        new NamedThreadFactory("worker-", false));
  }
}
