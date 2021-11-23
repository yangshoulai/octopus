package com.octopus.sample.wallhaven;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.utils.RateLimiter;
import java.io.File;

/**
 * 下载壁纸天堂壁纸 https://wallhaven.cc
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
public class WallpaperCrawler {

  public static void main(String[] args) {
    Octopus octopus =
        Octopus.builder()
            .autoStop(true)
            .setThreads(4)
            .addSite(WebSite.of("wallhaven.cc").setRateLimiter(RateLimiter.of(1, 10)))
            .addProcessor(new ListPageProcessor())
            .addProcessor(
                new MediaFileDownloadProcessor(
                    new File("/Users/yann/Downloads/wallpapers/wallhaven")))
            .build();
    octopus.addRequest(
        Request.get(
            "https://wallhaven.cc/search?categories=110&purity=100&ratios=16x9%2C16x10&sorting=hot&order=desc&page=3"));
    octopus.start();
  }
}
