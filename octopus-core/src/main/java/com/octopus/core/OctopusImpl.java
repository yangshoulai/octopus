package com.octopus.core;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.Downloader;
import com.octopus.core.exception.BadStatusException;
import com.octopus.core.exception.DownloadException;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.exception.ProcessorNotFoundException;
import com.octopus.core.listener.Listener;
import com.octopus.core.store.Store;
import com.octopus.core.utils.RequestHelper;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import java.util.stream.Collectors;
import lombok.NonNull;
import org.slf4j.Logger;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
class OctopusImpl implements Octopus {

  private final Logger logger;

  private final AtomicReference<State> state = new AtomicReference<>(State.NEW);

  private TimeInterval interval;

  private ExecutorService boss;

  private ExecutorService workers;

  private Semaphore workerSemaphore;

  private final Lock lock = new ReentrantLock();

  private final Condition idleCondition = lock.newCondition();

  private final int threads;

  private final Downloader downloader;

  private final Store store;

  private final List<WebSite> webSites;

  private final List<Listener> listeners;

  private final List<Processor> processors;

  private final DownloadConfig globalDownloadConfig;

  private final boolean autoStop;

  private final boolean clearStoreOnStartup;

  private final boolean clearStoreOnStop;

  private final boolean ignoreSeedsWhenStoreHasRequests;

  private final List<Request> seeds;

  private final boolean debug;

  private final String name;

  private final boolean replayFailedRequest;

  private final int maxReplays;

  private int replayTimes = 0;

  public OctopusImpl(OctopusBuilder builder) {
    this.logger = builder.getLogger();
    this.debug = builder.isDebug();
    this.name = builder.getName();
    this.seeds = builder.getSeeds();
    this.clearStoreOnStop = builder.isClearStoreOnStop();
    this.clearStoreOnStartup = builder.isClearStoreOnStartup();
    this.ignoreSeedsWhenStoreHasRequests = builder.isIgnoreSeedsWhenStoreHasRequests();
    this.autoStop = builder.isAutoStop();
    this.globalDownloadConfig = builder.getGlobalDownloadConfig();
    this.processors = builder.getProcessors();
    this.listeners = builder.getListeners();
    this.webSites = builder.getSites();
    this.store = builder.getStore();
    this.downloader = builder.getDownloader();
    this.threads = builder.getThreads();
    this.replayFailedRequest = builder.isReplayFailedRequest();
    this.maxReplays = builder.getMaxReplays();
  }

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
    if (!this.translateState(State.NEW, State.STARTING)) {
      throw new OctopusException("Illegal octopus state [" + this.state.get().getLabel() + "]");
    }
    if (this.debug && this.logger.isDebugEnabled()) {
      logger.debug("Octopus starting");
    }
    this.boss = this.createBossExecutor();
    this.workers = this.createWorkerExecutor();
    this.workerSemaphore = new Semaphore(this.threads);
    if (this.clearStoreOnStartup) {
      if (this.debug && this.logger.isDebugEnabled()) {
        logger.debug("Clear request store");
      }
      this.store.clear();
    }
    if (this.seeds != null) {
      if (!this.ignoreSeedsWhenStoreHasRequests || this.store.getWaitingSize() == 0) {
        this.seeds.forEach(this::addRequest);
      } else {
        if (this.debug && this.logger.isDebugEnabled()) {
          logger.debug("Request store has remaining requests, seeds will be ignored");
        }
      }
    }
    this.startRateLimiters();
    this.translateState(State.STARTING, State.STARTED);
    this.interval = new TimeInterval();
    logger.info(
        "Octopus started at [{}]", DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN));
    return CompletableFuture.runAsync(this::dispatch, this.boss);
  }

  @Override
  public void stop() throws OctopusException {
    State state = this.state.get();
    boolean isStarted = state == State.STARTED || state == State.IDLE;
    if (!isStarted || !this.translateState(state, State.STOPPING)) {
      throw new OctopusException("Illegal octopus state [" + this.state.get().getLabel() + "]");
    }
    if (this.debug && this.logger.isDebugEnabled()) {
      logger.debug("Octopus stopping");
    }
    this.boss.shutdown();
    this.workers.shutdown();
    this.stopRateLimiters();
    lock.lock();
    try {
      this.idleCondition.signalAll();
    } finally {
      lock.unlock();
    }
    this.translateState(State.STOPPING, State.STOPPED);

    long total = this.store.getTotalSize();
    long completed = this.store.getCompletedSize();
    long waiting = this.store.getWaitingSize();
    long failed = total - completed - waiting;
    if (this.clearStoreOnStop) {
      this.store.clear();
    }
    logger.info(
        "Total = [{}], completed = [{}], waiting = [{}], failed = [{}]",
        total,
        completed,
        waiting,
        failed);
    logger.info(
        "Octopus stopped at [{}] running [{}]",
        DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN),
        interval.intervalPretty());
  }

  @Override
  public void addRequest(@NonNull Request request) throws OctopusException {
    State state = this.state.get();
    if (state.getState() >= State.STOPPING.getState()) {
      throw new OctopusException("Illegal octopus state [" + this.state.get().getLabel() + "]");
    }
    request.setId(RequestHelper.generateId(request));
    if (state.getState() <= State.NEW.getState()) {
      this.seeds.add(request);
    } else {
      if (request.isRepeatable() || !this.store.exists(request)) {
        this.listeners.forEach(listener -> listener.beforeStore(request));
        if (this.store.put(request)) {
          if (this.translateState(State.IDLE, State.STARTED)) {
            lock.lock();
            try {
              this.idleCondition.signalAll();
            } finally {
              lock.unlock();
            }
          }
        } else {
          logger.error("Can not store request [{}]", request);
        }
      } else {
        if (this.debug && this.logger.isDebugEnabled()) {
          logger.debug("Ignore request [{}] as already exists", request);
        }
      }
    }
  }

  private void dispatch() {
    State state;
    while ((state = this.state.get()) == State.STARTED || state == State.IDLE) {
      try {
        Request request = this.store.get();
        if (request != null) {
          if (this.debug && this.logger.isDebugEnabled()) {
            logger.debug("Take request [{}] from store", request);
          }
          this.workerSemaphore.acquire();
          this.workers.execute(
              () -> {
                try {
                  this.listeners.forEach(listener -> listener.beforeDownload(request));
                  WebSite webSite = this.getTargetWebSite(request);
                  DownloadConfig downloadConfig = this.globalDownloadConfig;
                  if (webSite != null && webSite.getRateLimiter() != null) {
                    webSite.getRateLimiter().acquire();
                  }
                  if (webSite != null && webSite.getDownloadConfig() != null) {
                    downloadConfig = webSite.getDownloadConfig();
                  }
                  Response response = this.download(request, downloadConfig);
                  if (response != null) {
                    this.listeners.forEach(listener -> listener.beforeProcess(response));
                    if (!response.isSuccessful()) {
                      throw new BadStatusException(response);
                    }
                    this.process(response);
                  }
                  this.store.markAsCompleted(request);
                } catch (DownloadException e) {
                  this.store.markAsFailed(request);
                  this.listeners.forEach(listener -> listener.onDownloadError(request, e));
                  logger.error("Download [{}] error!", request, e);
                } catch (Throwable e) {
                  logger.error("", e);
                  this.store.markAsFailed(request);
                } finally {
                  this.workerSemaphore.release();
                  lock.lock();
                  try {
                    idleCondition.signalAll();
                  } finally {
                    lock.unlock();
                  }
                }
              });
        } else if (this.store.getWaitingSize() <= 0) {
          boolean wait = true;
          if (this.workerSemaphore.availablePermits() == this.threads) {
            if (this.replayFailedRequest && this.replayTimes < this.maxReplays) {
              this.replayTimes++;
              if (this.debug && this.logger.isDebugEnabled()) {
                logger.debug("Replay failed requests {} time", this.replayTimes);
              }
              List<Request> failed = this.store.getFailed();
              if (failed != null && !failed.isEmpty()) {
                if (this.debug && this.logger.isDebugEnabled()) {
                  logger.debug("Found [{}] failed requests", failed.size());
                }
                failed.forEach(
                    r -> {
                      r.setRepeatable(true);
                      this.addRequest(r);
                    });
                this.store.clearFailed();
                continue;
              } else {
                if (this.debug && this.logger.isDebugEnabled()) {
                  logger.debug("No failed request found");
                }
              }
            }
            if (this.autoStop) {
              logger.info("No more requests found, octopus will stop");
              this.stop();
              wait = false;
            } else {
              logger.info("No more requests found, octopus will idle");
              this.translateState(State.STARTED, State.IDLE);
            }

          } else {
            if (this.debug && this.logger.isDebugEnabled()) {
              logger.debug(
                  "No more requests found, waiting for [{}] running request complete",
                  (this.threads - this.workerSemaphore.availablePermits()));
            }
          }
          if (wait) {
            lock.lock();
            try {
              idleCondition.await();
            } finally {
              lock.unlock();
            }
          }
        }
      } catch (Throwable e) {
        logger.error("Error when dispatch request", e);
      }
    }
  }

  private Response download(Request request, DownloadConfig config) throws DownloadException {
    try {
      return this.downloader.download(request, config);
    } catch (Throwable e) {
      if (e instanceof DownloadException) {
        throw e;
      } else {
        throw new DownloadException(e);
      }
    }
  }

  private void process(Response response) {
    List<Processor> matchedProcessors =
        this.processors.stream().filter(p -> p.matches(response)).collect(Collectors.toList());
    if (matchedProcessors.isEmpty()) {
      throw new ProcessorNotFoundException(
          String.format("No processor found for request [%s]", response.getRequest().toString()));
    }
    for (Processor processor : matchedProcessors) {
      List<Request> newRequests = processor.process(response);
      this.listeners.forEach(listener -> listener.afterProcess(response, newRequests));
      if (newRequests != null) {
        newRequests.stream()
            .sorted()
            .forEach(
                request -> {
                  request.setParent(response.getRequest().getId());
                  if (request.isInherit() && response.getRequest().getAttributes() != null) {
                    Map<String, Object> attrs = response.getRequest().getAttributes();
                    attrs.forEach(
                        (k, v) -> {
                          if (request.getAttribute(k) == null) {
                            request.putAttribute(k, v);
                          }
                        });
                  }
                  this.addRequest(request);
                });
      }
    }
  }

  private void startRateLimiters() {
    if (this.debug && this.logger.isDebugEnabled()) {
      logger.debug("Start all rate limiters");
    }
    this.webSites.forEach(
        site -> {
          if (site.getRateLimiter() != null) {
            if (StrUtil.isBlank(site.getRateLimiter().getName())) {
              site.getRateLimiter().setName("rate-limiter/" + site.getHost());
            }
            site.getRateLimiter().start();
          }
        });
  }

  private void stopRateLimiters() {
    if (this.debug && this.logger.isDebugEnabled()) {
      logger.debug("Stop all rate limiters");
    }
    this.webSites.forEach(
        site -> {
          if (site.getRateLimiter() != null) {
            site.getRateLimiter().stop();
          }
        });
  }

  private boolean translateState(State from, State to) {
    if (this.state.compareAndSet(from, to)) {
      if (this.debug && this.logger.isDebugEnabled()) {
        logger.debug("State changed [{}] => [{}]", from.getLabel(), to.getLabel());
      }
      return true;
    }
    return false;
  }

  private WebSite getTargetWebSite(Request request) {
    return this.webSites.stream()
        .filter(webSite -> webSite.getHost().equals(URLUtil.url(request.getUrl()).getHost()))
        .findFirst()
        .orElse(null);
  }

  private ExecutorService createBossExecutor() {
    if (this.debug && this.logger.isDebugEnabled()) {
      logger.debug("Create boss executor [{}]", this.name + "-boss");
    }
    return new ThreadPoolExecutor(
        1,
        1,
        0,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        r -> {
          Thread t = new Thread(r);
          t.setName(this.name + "-boss");
          return t;
        });
  }

  private ExecutorService createWorkerExecutor() {
    if (this.debug && this.logger.isDebugEnabled()) {
      logger.debug("Create worker executors with size [{}]", this.threads);
    }
    return new ThreadPoolExecutor(
        this.threads,
        this.threads,
        0,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        new NamedThreadFactory(this.name + "-worker-", false));
  }
}
