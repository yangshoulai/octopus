package com.octopus.core;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.thread.NamedThreadFactory;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
class OctopusImpl implements Octopus {

  private final Logger log = LoggerFactory.getLogger("Octopus");

  private final AtomicReference<State> state = new AtomicReference<>(State.NEW);

  private TimeInterval interval;

  private ExecutorService boss;

  private ExecutorService workers;

  private int threads;

  private Semaphore workerSemaphore;

  private final Lock lock = new ReentrantLock();

  private final Condition idleCondition = lock.newCondition();

  private Downloader downloader;

  private Store store;

  private List<WebSite> webSites;

  private List<Listener> listeners;

  private List<Processor> processors;

  private DownloadConfig globalDownloadConfig;

  private boolean autoStop = false;

  private boolean clearStoreOnStartup = true;

  private List<Request> seeds = new ArrayList<>();

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
    log.info("Octopus starting");
    this.boss = this.createBossExecutor();
    this.workers = this.createWorkerExecutor();
    this.workerSemaphore = new Semaphore(this.threads);
    if (this.clearStoreOnStartup) {
      this.store.clear();
    }
    if (this.seeds != null) {
      this.seeds.forEach(this::addRequest);
    }
    this.startRateLimiters();
    this.translateState(State.STARTING, State.STARTED);
    this.interval = new TimeInterval();
    log.info(
        "Octopus started at [{}]", DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN));
    return CompletableFuture.runAsync(this::dispatch, this.boss);
  }

  @Override
  public void stop() throws OctopusException {
    State state = this.state.get();
    if ((state == State.STARTED || state == State.IDLE)
        && !this.translateState(state, State.STOPPING)) {
      throw new OctopusException("Illegal octopus state [" + this.state.get().getLabel() + "]");
    }
    log.info("Octopus stopping");
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

    int total = this.store.getTotalSize();
    int completed = this.store.getCompletedSize();
    int waiting = this.store.getWaitingSize();
    int failed = total - completed - waiting;
    log.info(
        "Total = [{}], completed = [{}], waiting = [{}], failed = [{}]",
        total,
        completed,
        waiting,
        failed);
    log.info(
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
          log.error("Can not store request [{}]", request);
        }
      } else {
        log.warn("Ignore request [{}] as already exist", request);
      }
    }
  }

  private void dispatch() {
    State state;
    while ((state = this.state.get()) == State.STARTED || state == State.IDLE) {
      try {
        Request request = this.store.get();
        if (request != null) {
          if (log.isDebugEnabled()) {
            log.debug(
                "Take request [{}] from store, remaining size [{}]",
                request,
                this.store.getWaitingSize());
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
                  this.store.markAsCompleted(request);
                  if (response != null) {
                    this.listeners.forEach(listener -> listener.beforeProcess(response));
                    if (!response.isSuccessful()) {
                      throw new BadStatusException(response);
                    }
                    this.process(response);
                  }
                } catch (DownloadException e) {
                  this.store.markAsFailed(request);
                  this.listeners.forEach(listener -> listener.onDownloadError(request, e));
                  log.error("Download [{}] error!", request, e);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                } catch (Throwable e) {
                  log.error("", e);
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
            if (this.autoStop) {
              log.info("No more requests found, octopus will stop");
              this.stop();
              wait = false;
            } else {
              log.info("No more requests found, octopus will idle");
              this.translateState(State.STARTED, State.IDLE);
            }

          } else {
            log.info(
                "No more requests found, waiting for [{}] running request complete",
                (this.threads - this.workerSemaphore.availablePermits()));
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
        log.error("Error when dispatch request", e);
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
    if (this.processors.stream().noneMatch(p -> p.matches(response))) {
      throw new ProcessorNotFoundException(
          String.format("No processor found for request [%s]", response.getRequest().toString()));
    }
    this.processors.stream()
        .filter(processor -> processor.matches(response))
        .forEach(
            processor -> {
              try {
                List<Request> newRequests = processor.process(response);
                this.listeners.forEach(listener -> listener.afterProcess(response, newRequests));
                if (newRequests != null) {
                  newRequests.stream()
                      .sorted()
                      .forEach(
                          request -> {
                            request.setParent(response.getRequest().getId());
                            this.addRequest(request);
                          });
                }
              } catch (Throwable e) {
                log.error(
                    "Error process request [{}] with processor [{}]",
                    response.getRequest(),
                    processor.getClass(),
                    e);
              }
            });
  }

  private void startRateLimiters() {
    this.webSites.forEach(
        site -> {
          if (site.getRateLimiter() != null) {
            site.getRateLimiter().start();
          }
        });
  }

  private void stopRateLimiters() {
    this.webSites.forEach(
        site -> {
          if (site.getRateLimiter() != null) {
            site.getRateLimiter().stop();
          }
        });
  }

  private boolean translateState(State from, State to) {
    if (this.state.compareAndSet(from, to)) {
      if (this.log.isDebugEnabled()) {
        log.debug("State changed [{}] => [{}]", from.getLabel(), to.getLabel());
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

  public void setGlobalDownloadConfig(DownloadConfig globalDownloadConfig) {
    this.globalDownloadConfig = globalDownloadConfig;
  }

  public void setThreads(int threads) {
    this.threads = threads;
  }

  public void setDownloader(Downloader downloader) {
    this.downloader = downloader;
  }

  public void setStore(Store store) {
    this.store = store;
  }

  public void setWebSites(List<WebSite> webSites) {
    this.webSites = webSites;
  }

  public void setListeners(List<Listener> listeners) {
    this.listeners = listeners;
  }

  public void setProcessors(List<Processor> processors) {
    this.processors = processors;
  }

  public void setAutoStop(boolean autoStop) {
    this.autoStop = autoStop;
  }

  public void setClearStoreOnStartup(boolean clearStoreOnStartup) {
    this.clearStoreOnStartup = clearStoreOnStartup;
  }

  public void setSeeds(List<Request> seeds) {
    this.seeds = seeds;
  }
}
