package com.octopus.sample.wallhaven;

import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/1
 */
@Slf4j
public class WallhavenOctopus {
  public static void main(String[] args) {
    Octopus.builder()
        .setName("wallhaven-spider")
        .debug()
        .setThreads(2)
        .useOkHttpDownloader()
        .addSite(WebSite.of("wallhaven.cc").setRateLimiter(1, 3))
        .addProcessor(WallhavenWallpaper.class)
        .addProcessor(new MediaFileDownloadProcessor("../../../downloads/wallpapers/wallhaven"))
        .addSeeds(
            "https://wallhaven.cc/search?categories=110&purity=100&ratios=16x9%2C16x10&topRange=1M&sorting=toplist&order=desc&page=1")
        .build()
        .start();
  }
}
