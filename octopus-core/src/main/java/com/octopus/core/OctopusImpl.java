package com.octopus.core;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpUtil;
import com.octopus.core.Request.Status;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.Downloader;
import com.octopus.core.exception.BadStatusException;
import com.octopus.core.exception.DownloadException;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.exception.ProcessorNotFoundException;
import com.octopus.core.logging.Logger;
import com.octopus.core.processor.Processor;
import com.octopus.core.processor.impl.MatchableProcessor;
import com.octopus.core.processor.jexl.JexlContextHolder;
import com.octopus.core.replay.ReplayFilter;
import com.octopus.core.replay.ReplayFilters;
import com.octopus.core.store.Store;
import com.octopus.core.utils.RequestHelper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
class OctopusImpl implements Octopus {

    private static final Pattern BASE_URL_PATTERN = Pattern.compile("^(https?://([^/]+)).*$");

    private final Logger logger;

    private final AtomicReference<State> state = new AtomicReference<>(State.NEW);
    private final Lock lock = new ReentrantLock();
    private final Condition idleCondition = lock.newCondition();
    private final int threads;
    private final Downloader downloader;
    private final Store store;
    private final List<WebSite> webSites;
    private final OctopusListenerNotifier listenerNotifier;
    private final List<MatchableProcessor> processors;
    private final DownloadConfig globalDownloadConfig;
    private final boolean autoStop;
    private final boolean clearStoreOnStartup;
    private final boolean clearStoreOnStop;
    private final boolean ignoreSeedsWhenStoreHasRequests;
    private final List<Request> seeds;
    private final String name;
    private final boolean replayFailedRequest;
    private final ReplayFilter replayFilter;
    private TimeInterval interval;
    private ExecutorService boss;
    private ExecutorService workers;
    private Semaphore workerSemaphore;
    private final int maxDepth;

    public OctopusImpl(OctopusBuilder builder) {
        this.logger = builder.getLogger();
        this.name = builder.getName();
        this.seeds = builder.getSeeds();
        this.clearStoreOnStop = builder.isClearStoreOnStop();
        this.clearStoreOnStartup = builder.isClearStoreOnStartup();
        this.ignoreSeedsWhenStoreHasRequests = builder.isIgnoreSeedsWhenStoreHasRequests();
        this.autoStop = builder.isAutoStop();
        this.globalDownloadConfig = builder.getGlobalDownloadConfig();
        this.processors = builder.getProcessors();
        this.listenerNotifier =
                new OctopusListenerNotifier(builder.getListeners(), builder.getLogger());
        this.webSites = builder.getSites();
        this.store = builder.getStore();
        this.downloader = builder.getDownloader();
        this.threads = builder.getThreads();
        this.replayFailedRequest = builder.isReplayFailedRequest();
        this.replayFilter =
                ReplayFilters.and(
                        builder.getReplayFilter(), r -> r.getFailTimes() - 1 < builder.getMaxReplays());
        this.maxDepth = builder.getMaxDepth();
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
            throw new OctopusException("Illegal state " + this.state.get());
        }
        if (this.logger.isDebugEnabled()) {
            logger.debug("Octopus starting");
        }
        this.boss = this.createBossExecutor();
        this.workers = this.createWorkerExecutor();
        this.workerSemaphore = new Semaphore(2 * this.threads);
        if (this.clearStoreOnStartup) {
            if (this.logger.isDebugEnabled()) {
                logger.debug("Clear request store");
            }
            this.store.clear();
        }
        if (!this.seeds.isEmpty()) {
            if (!this.ignoreSeedsWhenStoreHasRequests || this.store.getWaitingSize() == 0) {
                this.seeds.forEach(this::addRequest);
            } else {
                if (this.logger.isDebugEnabled()) {
                    logger.debug("Request store has remaining requests, seeds will be ignored");
                }
            }
        }
        this.startRateLimiters();
        this.translateState(State.STARTING, State.STARTED);
        this.interval = new TimeInterval();
        logger.info(
                "Octopus started at ["
                        + DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN)
                        + "]");
        return CompletableFuture.runAsync(this::dispatch, this.boss);
    }

    @Override
    public void stop() throws OctopusException {
        State state = this.state.get();
        boolean isStarted = state == State.STARTED || state == State.IDLE;
        if (!isStarted || !this.translateState(state, State.STOPPING)) {
            throw new OctopusException("Illegal state " + this.state.get());
        }
        if (this.logger.isDebugEnabled()) {
            logger.debug("Octopus stopping");
        }
        this.boss.shutdown();
        this.workers.shutdown();
        this.idleSignal();
        this.translateState(State.STOPPING, State.STOPPED);

        long total = this.store.getTotalSize();
        long completed = this.store.getCompletedSize();
        long waiting = this.store.getWaitingSize();
        long failed = this.store.getFailedSize();
        if (this.clearStoreOnStop) {
            this.store.clear();
        }
        logger.info(
                String.format(
                        "Total = [%s], Completed = [%s], Waiting = [%s], Failed = [%s]",
                        total, completed, waiting, failed));
        logger.info(
                String.format(
                        "Octopus stopped at [%s] running [%s]",
                        DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN),
                        interval.intervalPretty()));
    }

    @Override
    public void addRequest(@NonNull Request request) throws OctopusException {
        validateRequest(request);
        State state = this.state.get();
        if (state.getState() >= State.STOPPING.getState()) {
            throw new OctopusException("Illegal state " + this.state.get());
        }
        // request.setUrl(URLUtil.normalize(request.getUrl(), true));
        request.setId(RequestHelper.generateId(request));
        if (state.getState() <= State.NEW.getState()) {
            this.seeds.add(request);
        } else {
            if (request.isRepeatable() || !this.store.exists(request)) {
                this.listenerNotifier.beforeStore(request);
                request.setStatus(Status.of(Request.State.Waiting));
                if (this.store.put(request)) {
                    if (this.translateState(State.IDLE, State.STARTED)) {
                        this.idleSignal();
                    }
                } else {
                    logger.error(String.format("Can not store request [%s]", request));
                }
            } else {
                if (this.logger.isDebugEnabled()) {
                    logger.debug(String.format("Ignore request [%s] as already exists", request));
                }
            }
        }
    }

    private void dispatch() {
        State state;
        while (!Thread.currentThread().isInterrupted()
                && ((state = this.state.get()) == State.STARTED || state == State.IDLE)) {
            try {
                Request request = this.store.get();
                if (request != null) {
                    if (this.logger.isTraceEnabled()) {
                        logger.trace(String.format("Load request [%s] from store", request));
                    }
                    this.workerSemaphore.acquire();
                    this.workers.execute(new RequestTask(request));
                } else if (this.store.getWaitingSize() <= 0) {
                    boolean wait = true;
                    Disposable disposable = null;
                    if (this.workerSemaphore.availablePermits() == 2 * this.threads) {
                        if (this.replayFailedRequest && this.replyFailedRequests()) {
                            continue;
                        }
                        if (this.autoStop) {
                            logger.info("No more requests found, octopus will stop");
                            this.stop();
                            wait = false;
                        } else {
                            logger.info("No more requests found, octopus will idle");
                            this.translateState(State.STARTED, State.IDLE);
                            disposable = Observable.interval(5, TimeUnit.SECONDS, Schedulers.from(this.workers))
                                    .subscribe(l -> {
                                        long waiting = this.store.getWaitingSize();
                                        if (this.store.getWaitingSize() > 0) {
                                            logger.info("Found " + waiting + " waiting requests");
                                            this.idleSignal();
                                        }
                                    });
                        }

                    } else {
                        if (this.logger.isDebugEnabled()) {
                            logger.debug(
                                    String.format(
                                            "No more requests found, waiting for [%s] request to complete",
                                            (2 * this.threads - this.workerSemaphore.availablePermits())));
                        }
                    }
                    if (wait) {
                        this.idleWait();
                        if (disposable != null) {
                            disposable.dispose();
                        }
                    }
                }
            } catch (InterruptedException e) {
                this.stop();
            } catch (Throwable e) {
                logger.error("Error when dispatch request", e);
            }
        }
    }

    private void idleWait() throws InterruptedException {
        lock.lock();
        try {
            idleCondition.await();
        } finally {
            lock.unlock();
        }
    }

    private void idleSignal() {
        lock.lock();
        try {
            idleCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean replyFailedRequests() {
        int replaySize = this.store.replayFailed(this.replayFilter);
        if (this.logger.isDebugEnabled()) {
            logger.debug(String.format("Found [%s] failed requests to replay", replaySize));
        }
        return replaySize > 0;
    }


    private Response download(Request request, DownloadConfig config) throws DownloadException {
        try {
            return this.downloader.download(request, config);
        } catch (Throwable e) {
            if (e instanceof DownloadException) {
                throw e;
            } else {
                throw new DownloadException(
                        String.format("Download failed for request [%s], %s", request, e.getMessage()), e);
            }
        }
    }

    private void process(Response response) {
        List<Processor> matchedProcessors =
                this.processors.stream().filter(p -> p.matches(response)).collect(Collectors.toList());
        if (matchedProcessors.isEmpty()) {
            throw new ProcessorNotFoundException(response.getRequest());
        }
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(
                    String.format(
                            "Found [%s] matched processors for request [%s]",
                            matchedProcessors.size(), response.getRequest()));
        }
        for (Processor processor : matchedProcessors) {
            processor.process(response, new OctopusDelegate(response));
            this.listenerNotifier.afterProcess(response);

        }
    }

    private void addNewRequests(Request parentRequest, @NonNull Request request) {
        if (this.maxDepth >= 0 && parentRequest.getDepth() >= this.maxDepth) {
            logger.warn(String.format("Request[%s] is ignored as octopus reach max depth %s", request, this.maxDepth));
            return;
        }
        request.setParent(parentRequest.getId());
        if (request.isInherit() && parentRequest.getAttrs() != null) {
            Map<String, Object> attrs = parentRequest.getAttrs();
            attrs.forEach(
                    (k, v) -> {
                        if (request.getAttribute(k) == null) {
                            request.putAttribute(k, v);
                        }
                    });
        }
        if (!request.getHeaders().containsKey(Header.REFERER.getValue())) {
            String referer = ReUtil.get(BASE_URL_PATTERN, parentRequest.getUrl(), 1);
            request.getHeaders().put(Header.REFERER.getValue(), referer);
        }
        if (!HttpUtil.isHttp(request.getUrl()) && !HttpUtil.isHttps(request.getUrl())) {
            request.setUrl(URLUtil.completeUrl(parentRequest.getUrl(), request.getUrl()));
        }
        request.setDepth(parentRequest.getDepth() + 1);
        lock.lock();
        try {
            this.addRequest(request);
        } finally {
            lock.unlock();
        }
    }

    private void startRateLimiters() {
        if (this.logger.isDebugEnabled()) {
            logger.debug("Start all rate limiters");
        }
    }

    private boolean translateState(State from, State to) {
        if (this.state.compareAndSet(from, to)) {
            if (this.logger.isDebugEnabled()) {
                logger.debug(String.format("State changed [%s] => [%s]", from.getLabel(), to.getLabel()));
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
        if (this.logger.isDebugEnabled()) {
            logger.debug(String.format("Create boss executor [%s]", this.name + "/boss"));
        }
        return new ThreadPoolExecutor(
                1,
                1,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName(this.name + "/boss");
                    return t;
                });
    }

    private ExecutorService createWorkerExecutor() {
        if (this.logger.isDebugEnabled()) {
            logger.debug(String.format("Create worker executors with size [%s]", this.threads));
        }
        return new ThreadPoolExecutor(
                this.threads,
                this.threads,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory(this.name + "/worker-", false));
    }


    private void validateRequest(Request request) throws OctopusException {
        if (StrUtil.isBlank(request.getUrl())) {
            throw new OctopusException("Bad request as url is unset");
        }
        if (!HttpUtil.isHttp(request.getUrl()) && !HttpUtil.isHttps(request.getUrl())) {
            throw new OctopusException("Bad request url [" + request.getUrl() + "]");
        }
        if (request.getMethod() == null) {
            throw new OctopusException("Bad request as method is unset");
        }
    }

    private class RequestTask implements Runnable {

        private final Request request;

        private RequestTask(Request request) {
            this.request = request;
        }

        @Override
        public void run() {
            try {
                JexlContextHolder.setContext(new HashMap<>(16));
                JexlContextHolder.getContext().put(JexlContextHolder.KEY_REQUEST, request);
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Take request [%s]", request));
                }
                listenerNotifier.beforeDownload(request);
                WebSite webSite = getTargetWebSite(request);
                DownloadConfig downloadConfig = globalDownloadConfig;
                if (webSite != null && webSite.getRateLimiter() != null) {
                    webSite.getRateLimiter().acquire();
                }
                if (webSite != null && webSite.getDownloadConfig() != null) {
                    downloadConfig = webSite.getDownloadConfig();
                }
                Response response = null;
                if (request.isCache()) {
                    response = store.getResponse(request.getId());
                }
                if (response == null) {
                    response = download(request, downloadConfig);
                }
                if (response != null) {
                    JexlContextHolder.getContext().put(JexlContextHolder.KEY_RESPONSE, request);
                    listenerNotifier.beforeProcess(response);
                    if (!response.isSuccessful()) {
                        throw new BadStatusException(response);
                    }
                    if (request.isCache()) {
                        store.cacheResponse(response);
                    }
                    process(response);
                }
                store.markAsCompleted(request);
            } catch (Throwable e) {
                logger.error("", e);
                store.markAsFailed(request, e.getMessage());
                listenerNotifier.onError(request, e);
            } finally {
                JexlContextHolder.clear();
                workerSemaphore.release();
                idleSignal();
            }
        }
    }

    private class OctopusDelegate implements Octopus {

        private final Response response;

        private final AtomicInteger index = new AtomicInteger(0);

        private OctopusDelegate(Response response) {
            this.response = response;
        }

        @Override
        public void start() throws OctopusException {
            throw new OctopusException("Octopus already started");
        }

        @Override
        public Future<Void> startAsync() throws OctopusException {
            throw new OctopusException("Octopus already started");
        }

        @Override
        public void stop() throws OctopusException {
            OctopusImpl.this.stop();
        }

        @Override
        public void addRequest(Request request) throws OctopusException {
            request.setIndex(index.get());
            index.incrementAndGet();
            OctopusImpl.this.addNewRequests(response.getRequest(), request);
        }
    }


}
