package com.octopus.sample.wallhaven;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.matcher.Matchers;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 下载壁纸天堂壁纸 https://wallhaven.cc
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
@Slf4j
@Data
@Extractor(
    links = {
      @Link(
          selector =
              @Selector(expression = "#thumbs .thumb-listing-page ul li img", attr = "data-src")),
      @Link(
          selector =
              @Selector(expression = "ul.pagination li a.next", attr = "href", multi = false))
    })
public class WallhavenWallpaper {

  @Selector(expression = "#thumbs .thumb-listing-page ul li")
  private List<Wallpaper> wallpapers;

  @Data
  @Extractor
  public static class Wallpaper {

    @Selector(expression = "img", attr = "data-src")
    private String src;

    @Selector(expression = "a.preview", attr = "href")
    private String previewSrc;

    @Selector(expression = ".wall-res")
    private String resolution;

  }

  public static void main(String[] args) {
    List<Wallpaper> wallpapers = new ArrayList<>();
    Octopus octopus =
        Octopus.builder()
            .autoStop(true)
            .setThreads(4)
            .useOkHttpDownloader()
            .addSite(WebSite.of("wallhaven.cc").setRateLimiter(2, 10))
            .addProcessor(
                Matchers.HTML,
                WallhavenWallpaper.class,
                wallhavenWallpaper -> {
                  if (wallhavenWallpaper.getWallpapers() != null) {
                    wallpapers.addAll(wallhavenWallpaper.getWallpapers());
                  }
                })
            .addProcessor(
                new MediaFileDownloadProcessor("/Users/yann/Downloads/wallpapers/wallhaven/nsfw"))
            .build();
    octopus.addRequest(
        Request.get(
            "https://wallhaven.cc/search?categories=110&purity=100&ratios=16x9%2C16x10&sorting=hot&order=desc&page=1"));
    octopus.start();
    wallpapers.forEach(wallpaper -> log.debug("{}", wallpaper));
  }
}
