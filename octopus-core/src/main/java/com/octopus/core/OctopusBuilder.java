package com.octopus.core;

import com.octopus.core.downloader.CommonDownloadConfig;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.Downloader;
import com.octopus.core.downloader.HttpClientDownloader;
import com.octopus.core.listener.Listener;
import com.octopus.core.processor.LoggerProcessor;
import com.octopus.core.store.MemoryStore;
import com.octopus.core.store.Store;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

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

  private boolean autoStop = false;

  public OctopusBuilder setDownloader(@NonNull Downloader downloader) {
    this.downloader = downloader;
    return this;
  }

  public OctopusBuilder setStore(Store store) {
    this.store = store;
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
    return octopus;
  }
}
