package com.octopus.core;

import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.mongodb.MongoClient;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.Downloader;
import com.octopus.core.downloader.HttpClientDownloader;
import com.octopus.core.downloader.OkHttpDownloader;
import com.octopus.core.logging.Logger;
import com.octopus.core.logging.LoggerFactory;
import com.octopus.core.processor.ExtractorProcessor;
import com.octopus.core.processor.LoggerProcessor;
import com.octopus.core.processor.MatchableProcessor;
import com.octopus.core.processor.MatchedProcessor;
import com.octopus.core.processor.Processor;
import com.octopus.core.processor.extractor.Collector;
import com.octopus.core.processor.matcher.Matcher;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.core.replay.ReplayFilter;
import com.octopus.core.replay.ReplayFilters;
import com.octopus.core.store.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.NonNull;
import redis.clients.jedis.JedisPool;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class OctopusBuilder {

    private final List<WebSite> sites = new ArrayList<>();
    private final List<OctopusListener> listeners = new ArrayList<>();
    private final List<MatchableProcessor> processors = new ArrayList<>();
    private final List<Request> seeds = new ArrayList<>();
    private Downloader downloader = new HttpClientDownloader();
    private Store store = new MemoryStore();
    private int threads = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * 全局下载配置
     */
    private DownloadConfig globalDownloadConfig = new DownloadConfig();
    /**
     * 请求处理完毕后是否关闭爬虫
     */
    private boolean autoStop = true;
    /**
     * 开始时是否清空请求存储器
     */
    private boolean clearStoreOnStartup = true;
    /**
     * 结束时是否清空请求存储器
     */
    private boolean clearStoreOnStop = true;
    /**
     * 当存在未完成的下载请求时是否忽略种子请求
     *
     * <p>用于继续前一次未完成的爬虫任务
     */
    private boolean ignoreSeedsWhenStoreHasRequests = true;
    /**
     * 当所有请求处理完毕以后是否重放失败的请求
     */
    private boolean replayFailedRequest = true;
    private ReplayFilter replayFilter = ReplayFilters.all();
    /**
     * 重放失败请求的次数，默认 1
     *
     * <p>超过该值则爬虫会停止或者进入等待状态
     */
    private int maxReplays = 1;
    private String name = "Octopus";

    private Logger logger = LoggerFactory.getLogger(Octopus.class.getName());

    /**
     * 使用 HttpClientDownloader
     *
     * @return OctopusBuilder
     * @see com.octopus.core.downloader.HttpClientDownloader
     */
    public OctopusBuilder useHttpClientDownloader() {
        this.downloader = new HttpClientDownloader();
        return this;
    }

    /**
     * 使用 OkHttpDownloader
     *
     * @return OctopusBuilder
     * @see com.octopus.core.downloader.OkHttpDownloader
     */
    public OctopusBuilder useOkHttpDownloader() {
        this.downloader = new OkHttpDownloader();
        return this;
    }

    /**
     * 使用本地文件存储请求
     *
     * @param dir 文件夹
     * @return OctopusBuilder
     */
    public OctopusBuilder useFsStore(@NonNull File dir) {
        this.store = new FileSystemStore(dir);
        return this;
    }

    /**
     * 使用本地文件存储请求
     *
     * @param dir 文件夹
     * @return OctopusBuilder
     */
    public OctopusBuilder useFsStore(@NonNull String dir) {
        this.store = new FileSystemStore(dir);
        return this;
    }

    /**
     * 设置 Redis 请求存储器
     *
     * @param pool jedisPool
     * @return OctopusBuilder
     * @see com.octopus.core.store.RedisStore
     */
    public OctopusBuilder useRedisStore(@NonNull JedisPool pool) {
        this.store = new RedisStore(pool);
        return this;
    }

    /**
     * 使用 Redis 请求存储器
     *
     * @param keyPrefix redis 键名前缀
     * @param pool      jedisPool
     * @return OctopusBuilder
     * @see com.octopus.core.store.RedisStore
     */
    public OctopusBuilder useRedisStore(@NonNull String keyPrefix, @NonNull JedisPool pool) {
        this.store = new RedisStore(keyPrefix, pool);
        return this;
    }

    /**
     * 使用 本机 Redis 请求存储器
     *
     * @return OctopusBuilder
     * @see com.octopus.core.store.RedisStore
     */
    public OctopusBuilder useRedisStore() {
        this.store = new RedisStore();
        return this;
    }

    /**
     * 使用 本机 Redis 请求存储器
     *
     * @param keyPrefix redis 键名前缀
     * @return OctopusBuilder
     * @see com.octopus.core.store.RedisStore
     */
    public OctopusBuilder useRedisStore(@NonNull String keyPrefix) {
        this.store = new RedisStore(keyPrefix, new JedisPool());
        return this;
    }

    /**
     * 使用内存请求存储器
     *
     * @return OctopusBuilder
     * @see com.octopus.core.store.MemoryStore
     */
    public OctopusBuilder useMemoryStore() {
        this.store = new MemoryStore();
        return this;
    }

    /**
     * 使用 Mongo 请求存储器
     *
     * @param database    数据库名称
     * @param collection  集合名称
     * @param mongoClient 客户端
     * @return OctopusBuilder
     * @see com.octopus.core.store.MongoStore
     */
    public OctopusBuilder useMongoStore(
            @NonNull String database, @NonNull String collection, @NonNull MongoClient mongoClient) {
        this.store = new MongoStore(database, collection, mongoClient);
        return this;
    }

    /**
     * 使用 Mongo 请求存储器
     *
     * @param mongoClient 客户端
     * @return OctopusBuilder
     * @see com.octopus.core.store.MongoStore
     */
    public OctopusBuilder useMongoStore(@NonNull MongoClient mongoClient) {
        this.store = new MongoStore(mongoClient);
        return this;
    }

    /**
     * 使用 Mongo 请求存储器
     *
     * @param database   数据库名称
     * @param collection 集合名称
     * @return OctopusBuilder
     * @see com.octopus.core.store.MongoStore
     */
    public OctopusBuilder useMongoStore(@NonNull String database, @NonNull String collection) {
        this.store = new MongoStore(database, collection);
        return this;
    }

    /**
     * 使用 Mongo 请求存储器
     *
     * @return OctopusBuilder
     * @see com.octopus.core.store.MongoStore
     */
    public OctopusBuilder useMongoStore() {
        this.store = new MongoStore();
        return this;
    }

    /**
     * 使用 ES 存储请求
     *
     * @param client    客户端
     * @param indexName 索引名
     * @return OctopusBuilder
     */
    public OctopusBuilder useEsStore(ElasticsearchClient client, String indexName) {
        this.store = new ElasticsearchStore(client, indexName);
        return this;
    }

    /**
     * 添加爬虫站点配置
     *
     * @param site 站点配置
     * @return OctopusBuilder
     */
    public OctopusBuilder addSite(@NonNull WebSite site) {
        if (site.getRateLimiter() != null && StrUtil.isBlank(site.getRateLimiter().getName())) {
            site.getRateLimiter().setName("rate-limiter/" + site.getHost());
        }
        this.sites.add(site);
        return this;
    }

    /**
     * 添加请求监听器
     *
     * @param listener 监听器
     * @return OctopusBuilder
     */
    public OctopusBuilder addListener(@NonNull OctopusListener listener) {
        this.listeners.add(listener);
        return this;
    }

    /**
     * 添加响应处理器
     *
     * @param processor 响应处理器
     * @return OctopusBuilder
     */
    public OctopusBuilder addProcessor(@NonNull Processor processor) {
        if (processor instanceof MatchableProcessor) {
            this.processors.add((MatchableProcessor) processor);
            return this;
        } else {
            return this.addProcessor(Matchers.ALL, processor);
        }
    }

    /**
     * 添加响应处理器
     *
     * @param processor 响应处理器
     * @return OctopusBuilder
     */
    public OctopusBuilder addProcessor(@NonNull Matcher matcher, @NonNull Processor processor) {
        this.processors.add(new MatchedProcessor(matcher, processor));
        return this;
    }

    /**
     * 添加响应处理器
     *
     * @param extractorClass 响应提取器
     * @return OctopusBuilder
     */
    public <T> OctopusBuilder addProcessor(@NonNull Class<T> extractorClass) {
        return this.addProcessor(new ExtractorProcessor<>(extractorClass));
    }

    /**
     * 添加响应处理器
     *
     * @param matcher        匹配器
     * @param extractorClass 响应提取器
     * @return OctopusBuilder
     */
    public <T> OctopusBuilder addProcessor(
            @NonNull Matcher matcher, @NonNull Class<T> extractorClass) {
        return this.addProcessor(matcher, extractorClass, null);
    }

    /**
     * 添加响应处理器
     *
     * @param matcher        匹配器
     * @param extractorClass 响应提取器
     * @param callback       回调
     * @param <T>            提取内容
     * @return OctopusBuilder
     */
    public <T> OctopusBuilder addProcessor(
            @NonNull Matcher matcher, @NonNull Class<T> extractorClass, Collector<T> callback) {
        return this.addProcessor(matcher, new ExtractorProcessor<>(extractorClass, callback));
    }

    /**
     * 结束后自动关闭
     *
     * @return OctopusBuilder
     */
    public OctopusBuilder autoStop() {
        return this.autoStop(true);
    }

    /**
     * 爬虫引擎结束后是否自动关闭
     *
     * @param autoStop 是否自动关闭
     * @return OctopusBuilder
     */
    public OctopusBuilder autoStop(boolean autoStop) {
        this.autoStop = autoStop;
        return this;
    }

    /**
     * 启动时自动清空请求管理器
     *
     * @return OctopusBuilder
     */
    public OctopusBuilder clearStoreOnStartup() {
        return this.clearStoreOnStartup(true);
    }

    /**
     * 启动时是否自动清空请求管理器
     *
     * @param clearStoreOnStartup 是否自动清空请求管理器
     * @return OctopusBuilder
     */
    public OctopusBuilder clearStoreOnStartup(boolean clearStoreOnStartup) {
        this.clearStoreOnStartup = clearStoreOnStartup;
        return this;
    }

    /**
     * 停止时是否自动清空请求管理器
     *
     * @param clearStoreOnStop 是否自动清空请求管理器
     * @return OctopusBuilder
     */
    public OctopusBuilder clearStoreOnStop(boolean clearStoreOnStop) {
        this.clearStoreOnStop = clearStoreOnStop;
        return this;
    }

    /**
     * 停止时自动清空请求管理器
     *
     * @return OctopusBuilder
     */
    public OctopusBuilder clearStoreOnStop() {
        return this.clearStoreOnStop(true);
    }

    /**
     * 如果请求管理器存在未处理完的请求是否忽略种子请求
     *
     * @return OctopusBuilder
     */
    public OctopusBuilder ignoreSeedsWhenStoreHasRequests() {
        return this.ignoreSeedsWhenStoreHasRequests(true);
    }

    /**
     * 如果请求管理器存在未处理完的请求是否忽略种子请求
     *
     * @param ignore 是否忽略种子请求
     * @return OctopusBuilder
     */
    public OctopusBuilder ignoreSeedsWhenStoreHasRequests(boolean ignore) {
        this.ignoreSeedsWhenStoreHasRequests = ignore;
        return this;
    }

    /**
     * 添加种子请求
     *
     * @param seeds 种子请求
     * @return OctopusBuilder
     */
    public OctopusBuilder addSeeds(@NonNull Request... seeds) {
        Arrays.stream(seeds).sorted().forEach(this.seeds::add);
        return this;
    }

    /**
     * 添加种子请求
     *
     * @param seeds 种子请求
     * @return OctopusBuilder
     */
    public OctopusBuilder addSeeds(@NonNull String... seeds) {
        Arrays.stream(seeds).sorted().forEach(seed -> this.seeds.add(Request.get(seed)));
        return this;
    }

    public Downloader getDownloader() {
        return downloader;
    }

    /**
     * 设置请求下载器
     *
     * @param downloader 请求下载器
     * @return OctopusBuilder
     * @see com.octopus.core.downloader.Downloader
     */
    public OctopusBuilder setDownloader(@NonNull Downloader downloader) {
        this.downloader = downloader;
        return this;
    }

    public Store getStore() {
        return store;
    }

    /**
     * 设置请求存储器
     *
     * @param store 请求存储器
     * @return OctopusBuilder
     * @see com.octopus.core.store.Store
     */
    public OctopusBuilder setStore(@NonNull Store store) {
        this.store = store;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    /**
     * 设置引擎工作线程数
     *
     * <p>默认 cpu核心数 x 2
     *
     * @param threads 工作线程数
     * @return OctopusBuilder
     */
    public OctopusBuilder setThreads(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must > 0");
        }
        this.threads = threads;
        return this;
    }

    public List<WebSite> getSites() {
        return sites;
    }

    public List<OctopusListener> getListeners() {
        return listeners;
    }

    public List<MatchableProcessor> getProcessors() {
        return processors;
    }

    public DownloadConfig getGlobalDownloadConfig() {
        return globalDownloadConfig;
    }

    /**
     * 设置全局下载配置
     *
     * @param globalDownloadConfig 全局下载配置
     * @return OctopusBuilder
     */
    public OctopusBuilder setGlobalDownloadConfig(@NonNull DownloadConfig globalDownloadConfig) {
        this.globalDownloadConfig = globalDownloadConfig;
        return this;
    }

    public boolean isAutoStop() {
        return autoStop;
    }

    public boolean isClearStoreOnStartup() {
        return clearStoreOnStartup;
    }

    public boolean isClearStoreOnStop() {
        return clearStoreOnStop;
    }

    public List<Request> getSeeds() {
        return seeds;
    }

    public String getName() {
        return name;
    }

    /**
     * 设置爬虫名称
     *
     * <p>主要区分系统存在多个爬虫
     *
     * @param name 爬虫名称
     * @return OctopusBuilder
     */
    public OctopusBuilder setName(@NonNull String name) {
        this.name = name;
        return this;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * 设置爬虫日志
     *
     * @param logger 日志
     * @return OctopusBuilder
     */
    public OctopusBuilder setLogger(@NonNull Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * 设置爬虫日志
     *
     * @param cls 日志
     * @return OctopusBuilder
     */
    public OctopusBuilder setLogger(@NonNull Class<?> cls) {
        return this.setLogger(LoggerFactory.getLogger(cls));
    }

    /**
     * 设置爬虫日志
     *
     * @param logger 日志
     * @return OctopusBuilder
     */
    public OctopusBuilder setLogger(@NonNull String logger) {
        return this.setLogger(LoggerFactory.getLogger(logger));
    }

    public boolean isIgnoreSeedsWhenStoreHasRequests() {
        return ignoreSeedsWhenStoreHasRequests;
    }

    public boolean isReplayFailedRequest() {
        return replayFailedRequest;
    }

    /**
     * 请求处理完成后是否重放失败的请求继续处理
     *
     * @param replayFailedRequest 是否重放失败的请求继续处理
     * @return OctopusBuilder
     */
    public OctopusBuilder setReplayFailedRequest(boolean replayFailedRequest) {
        this.replayFailedRequest = replayFailedRequest;
        return this;
    }

    public ReplayFilter getReplayFilter() {
        return replayFilter;
    }

    /**
     * 设置请求重放过滤器
     *
     * @param replayFilter 重放过滤器
     */
    public OctopusBuilder setReplayFilter(@NonNull ReplayFilter replayFilter) {
        this.replayFilter = replayFilter;
        return this;
    }

    public int getMaxReplays() {
        return maxReplays;
    }

    /**
     * 设置最大失败请求重放次数
     *
     * @param maxReplays 最大重放次数
     * @return OctopusBuilder
     */
    public OctopusBuilder setMaxReplays(int maxReplays) {
        this.maxReplays = maxReplays;
        return this;
    }

    public Octopus build() {
        if (this.processors.isEmpty()) {
            processors.add(new LoggerProcessor(Matchers.ALL));
        }

        return new OctopusImpl(this);
    }
}
