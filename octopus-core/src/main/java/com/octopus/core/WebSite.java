package com.octopus.core;

import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.utils.RateLimiter;

/**
 * 目标网站信息
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class WebSite {

  /** 网站域名 */
  private String host;

  /** 网站爬虫速率限制 */
  private RateLimiter rateLimiter;

  /** 网站的下载配置 */
  private DownloadConfig downloadConfig;

  public String getHost() {
    return host;
  }

  public WebSite setHost(String host) {
    this.host = host;
    return this;
  }

  public RateLimiter getRateLimiter() {
    return rateLimiter;
  }

  public WebSite setRateLimiter(RateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
    return this;
  }

  public DownloadConfig getDownloadConfig() {
    return downloadConfig;
  }

  public WebSite setDownloadConfig(DownloadConfig downloadConfig) {
    this.downloadConfig = downloadConfig;
    return this;
  }
}
