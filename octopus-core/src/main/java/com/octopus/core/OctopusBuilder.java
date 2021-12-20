package com.octopus.core;

import com.mongodb.MongoClient;
import com.octopus.core.downloader.CommonDownloadConfig;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.Downloader;
import com.octopus.core.downloader.HttpClientDownloader;
import com.octopus.core.downloader.OkHttpDownloader;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.extractor.ExtractorHelper;
import com.octopus.core.extractor.InvalidExtractorException;
import com.octopus.core.extractor.Result;
import com.octopus.core.listener.Listener;
import com.octopus.core.processor.AbstractProcessor;
import com.octopus.core.processor.LoggerProcessor;
import com.octopus.core.processor.matcher.Matcher;
import com.octopus.core.store.MemoryStore;
import com.octopus.core.store.MongoStore;
import com.octopus.core.store.RedisStore;
import com.octopus.core.store.Store;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class OctopusBuilder {

  private Downloader downloader = new HttpClientDownloader();

  private Store store = new MemoryStore();

  private int threads = Runtime.getRuntime().availableProcessors() * 2;

  private final List<WebSite> sites = new ArrayList<>();

  private final List<Listener> listeners = new ArrayList<>();

  private final List<Processor> processors = new ArrayList<>();

  private DownloadConfig globalDownloadConfig = new CommonDownloadConfig();

  private boolean autoStop = true;

  private boolean clearStoreOnStartup = true;

  private boolean clearStoreOnStop = true;

  private boolean ignoreSeedsWhenStoreHasRequests = true;

  private final List<Request> seeds = new ArrayList<>();

  private boolean debug = false;

  private String name = "octopus";

  private Logger logger = LoggerFactory.getLogger("Octopus");

  public OctopusBuilder setLogger(@NonNull Logger logger) {
    this.logger = logger;
    return this;
  }

  public OctopusBuilder setLogger(@NonNull Class<?> cls) {
    return this.setLogger(LoggerFactory.getLogger(cls));
  }

  public OctopusBuilder setLogger(@NonNull String logger) {
    return this.setLogger(LoggerFactory.getLogger(logger));
  }

  public OctopusBuilder debug(boolean debug) {
    this.debug = debug;
    return this;
  }

  public OctopusBuilder debug() {
    return this.debug(true);
  }

  public OctopusBuilder setDownloader(@NonNull Downloader downloader) {
    this.downloader = downloader;
    return this;
  }

  public OctopusBuilder useHttpClientDownloader() {
    this.downloader = new HttpClientDownloader();
    return this;
  }

  public OctopusBuilder useOkHttpDownloader() {
    this.downloader = new OkHttpDownloader();
    return this;
  }

  public OctopusBuilder setStore(@NonNull Store store) {
    this.store = store;
    return this;
  }

  public OctopusBuilder useRedisStore(@NonNull JedisPool pool) {
    this.store = new RedisStore(pool);
    return this;
  }

  public OctopusBuilder useRedisStore(@NonNull String keyPrefix, @NonNull JedisPool pool) {
    this.store = new RedisStore(keyPrefix, pool);
    return this;
  }

  public OctopusBuilder useRedisStore() {
    this.store = new RedisStore();
    return this;
  }

  public OctopusBuilder useRedisStore(@NonNull String keyPrefix) {
    this.store = new RedisStore(keyPrefix, new JedisPool());
    return this;
  }

  public OctopusBuilder useMemoryStore() {
    this.store = new MemoryStore();
    return this;
  }

  public OctopusBuilder useMongoStore(
      @NonNull String database, @NonNull String collection, @NonNull MongoClient mongoClient) {
    this.store = new MongoStore(database, collection, mongoClient);
    return this;
  }

  public OctopusBuilder useMongoStore(@NonNull MongoClient mongoClient) {
    this.store = new MongoStore(mongoClient);
    return this;
  }

  public OctopusBuilder useMongoStore(@NonNull String database, @NonNull String collection) {
    this.store = new MongoStore(database, collection);
    return this;
  }

  public OctopusBuilder useMongoStore() {
    this.store = new MongoStore();
    return this;
  }

  public OctopusBuilder setThreads(int threads) {
    if (threads <= 0) {
      throw new IllegalArgumentException("threads must > 0");
    }
    this.threads = threads;
    return this;
  }

  public OctopusBuilder addSite(@NonNull WebSite site) {
    this.sites.add(site);
    return this;
  }

  public OctopusBuilder addListener(@NonNull Listener listener) {
    this.listeners.add(listener);
    return this;
  }

  public OctopusBuilder addProcessor(@NonNull Processor processor) {
    this.processors.add(processor);
    return this;
  }

  public <T> OctopusBuilder addProcessor(@NonNull Class<T> extractorClass) {
    return this.addProcessor(null, extractorClass);
  }

  public <T> OctopusBuilder addProcessor(@NonNull Class<T> extractorClass, Consumer<T> callback) {
    return this.addProcessor(null, extractorClass, callback);
  }

  public <T> OctopusBuilder addProcessor(Matcher matcher, @NonNull Class<T> extractorClass) {
    return this.addProcessor(matcher, extractorClass, null);
  }

  public <T> OctopusBuilder addProcessor(
      Matcher matcher, @NonNull Class<T> extractorClass, Consumer<T> callback) {
    if (!ExtractorHelper.checkIsValidExtractorClass(extractorClass)) {
      throw new InvalidExtractorException("Not a valid extractor class");
    }
    if (matcher == null) {
      matcher = ExtractorHelper.extractMatcher(extractorClass);
    }
    this.processors.add(
        new AbstractProcessor(matcher) {
          @Override
          public List<Request> process(Response response) {
            try {
              Result<T> result = ExtractorHelper.extract(response, extractorClass);
              if (callback != null) {
                callback.accept(result.getObj());
              }
              return result.getRequests();
            } catch (Exception e) {
              throw new ProcessException(
                  "Error process response from request ["
                      + response.getRequest()
                      + "] with extractor "
                      + extractorClass.getName(),
                  e);
            }
          }
        });
    return this;
  }

  public OctopusBuilder setGlobalDownloadConfig(@NonNull DownloadConfig globalDownloadConfig) {
    this.globalDownloadConfig = globalDownloadConfig;
    return this;
  }

  public OctopusBuilder autoStop() {
    return this.autoStop(true);
  }

  public OctopusBuilder autoStop(boolean autoStop) {
    this.autoStop = autoStop;
    return this;
  }

  public OctopusBuilder clearStoreOnStartup() {
    return this.clearStoreOnStartup(true);
  }

  public OctopusBuilder clearStoreOnStartup(boolean clearStoreOnStartup) {
    this.clearStoreOnStartup = clearStoreOnStartup;
    return this;
  }

  public OctopusBuilder clearStoreOnStop(boolean clearStoreOnStop) {
    this.clearStoreOnStop = clearStoreOnStop;
    return this;
  }

  public OctopusBuilder clearStoreOnStop() {
    return this.clearStoreOnStop(true);
  }

  public OctopusBuilder ignoreSeedsWhenStoreHasRequests() {
    return this.ignoreSeedsWhenStoreHasRequests(true);
  }

  public OctopusBuilder ignoreSeedsWhenStoreHasRequests(boolean ignore) {
    this.ignoreSeedsWhenStoreHasRequests = ignore;
    return this;
  }

  public OctopusBuilder addSeeds(@NonNull Request... seeds) {
    Arrays.stream(seeds).sorted().forEach(this.seeds::add);
    return this;
  }

  public OctopusBuilder addSeeds(@NonNull String... seeds) {
    Arrays.stream(seeds).sorted().forEach(seed -> this.seeds.add(Request.get(seed)));
    return this;
  }

  public OctopusBuilder setName(@NonNull String name) {
    this.name = name;
    return this;
  }

  public Downloader getDownloader() {
    return downloader;
  }

  public Store getStore() {
    return store;
  }

  public int getThreads() {
    return threads;
  }

  public List<WebSite> getSites() {
    return sites;
  }

  public List<Listener> getListeners() {
    return listeners;
  }

  public List<Processor> getProcessors() {
    return processors;
  }

  public DownloadConfig getGlobalDownloadConfig() {
    return globalDownloadConfig;
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

  public boolean isDebug() {
    return debug;
  }

  public String getName() {
    return name;
  }

  public Logger getLogger() {
    return logger;
  }

  public boolean isIgnoreSeedsWhenStoreHasRequests() {
    return ignoreSeedsWhenStoreHasRequests;
  }

  public Octopus build() {
    if (this.processors.isEmpty()) {
      processors.add(new LoggerProcessor());
    }
    return new OctopusImpl(this);
  }
}
