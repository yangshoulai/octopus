package com.octopus.sample.wallhaven;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.sample.wallhaven.WallhavenWallpaper.Wallpaper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/1
 */
@Slf4j
public class WallhavenOctopus {
  public static void main(String[] args) {
    List<Wallpaper> wallpapers = new ArrayList<>();
    Octopus octopus =
        Octopus.builder()
            .setThreads(4)
            .useOkHttpDownloader()
            .addSite(WebSite.of("wallhaven.cc").setRateLimiter(2, 10))
            .addProcessor(
                WallhavenWallpaper.class,
                wallhavenWallpaper -> {
                  if (wallhavenWallpaper.getWallpapers() != null) {
                    wallpapers.addAll(wallhavenWallpaper.getWallpapers());
                  }
                })
            .addProcessor(new MediaFileDownloadProcessor("../../../downloads/wallpapers/wallhaven"))
            .build();
    octopus.addRequest(
        Request.get(
            "https://wallhaven.cc/search?categories=110&purity=100&ratios=16x9%2C16x10&sorting=hot&order=desc&page=1"));
    octopus.start();
    wallpapers.forEach(wallpaper -> log.debug("{}", wallpaper));
  }
}
