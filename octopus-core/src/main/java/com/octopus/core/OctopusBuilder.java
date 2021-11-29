package com.octopus.core;

import com.octopus.core.downloader.CommonDownloadConfig;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.Downloader;
import com.octopus.core.downloader.HttpClientDownloader;
import com.octopus.core.downloader.OkHttpDownloader;
import com.octopus.core.extractor.ExtractorHelper;
import com.octopus.core.extractor.InvalidExtractorException;
import com.octopus.core.extractor.Result;
import com.octopus.core.listener.Listener;
import com.octopus.core.processor.AbstractProcessor;
import com.octopus.core.processor.LoggerProcessor;
import com.octopus.core.processor.matcher.Matcher;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.core.store.MemoryStore;
import com.octopus.core.store.RedisStore;
import com.octopus.core.store.Store;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import lombok.NonNull;
import redis.clients.jedis.JedisPool;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class OctopusBuilder {

  private Downloader downloader;

  private Store store;

  private int threads = Runtime.getRuntime().availableProcessors() * 2;

  private final List<WebSite> sites = new ArrayList<>();

  private final List<Listener> listeners = new ArrayList<>();

  private final List<Processor> processors = new ArrayList<>();

  private DownloadConfig globalDownloadConfig;

  private boolean autoStop = true;

  private boolean clearStoreOnStartup = true;

  private final List<Request> seeds = new ArrayList<>();

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

  public OctopusBuilder setStore(Store store) {
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
    return this.addProcessor(Matchers.ALL, extractorClass);
  }

  public <T> OctopusBuilder addProcessor(@NonNull Class<T> extractorClass, Consumer<T> callback) {
    return this.addProcessor(Matchers.ALL, extractorClass, callback);
  }

  public <T> OctopusBuilder addProcessor(
      @NonNull Matcher matcher, @NonNull Class<T> extractorClass) {
    return this.addProcessor(matcher, extractorClass, null);
  }

  public <T> OctopusBuilder addProcessor(
      @NonNull Matcher matcher, @NonNull Class<T> extractorClass, Consumer<T> callback) {
    if (!ExtractorHelper.checkIsValidExtractorClass(extractorClass)) {
      throw new InvalidExtractorException("Not a valid extractor class");
    }
    this.processors.add(
        new AbstractProcessor(matcher) {
          @Override
          public List<Request> process(Response response) {
            Result<T> result =
                ExtractorHelper.extract(
                    response.getRequest().getUrl(), response.asText(), extractorClass);
            if (callback != null) {
              callback.accept(result.getObj());
            }
            return result.getRequests();
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

  public OctopusBuilder addSeeds(@NonNull Request... seeds) {
    Arrays.stream(seeds).sorted().forEach(this.seeds::add);
    return this;
  }

  public OctopusBuilder addSeeds(@NonNull String... seeds) {
    Arrays.stream(seeds).sorted().forEach(seed -> this.seeds.add(Request.get(seed)));
    return this;
  }

  public Octopus build() {
    OctopusImpl octopus = new OctopusImpl();
    octopus.setDownloader(this.downloader == null ? new HttpClientDownloader() : this.downloader);
    octopus.setStore(this.store == null ? new MemoryStore() : this.store);
    octopus.setThreads(this.threads);
    octopus.setWebSites(this.sites);
    octopus.setListeners(this.listeners);
    if (this.processors.isEmpty()) {
      processors.add(new LoggerProcessor());
    }
    octopus.setProcessors(this.processors);
    octopus.setGlobalDownloadConfig(
        this.globalDownloadConfig == null ? new CommonDownloadConfig() : null);
    octopus.setAutoStop(this.autoStop);
    octopus.setClearStoreOnStartup(this.clearStoreOnStartup);
    octopus.setSeeds(this.seeds);
    return octopus;
  }
}
