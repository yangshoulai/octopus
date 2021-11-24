package com.octopus.sample.wallhaven;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.annotation.CssSelector;
import com.octopus.core.processor.annotation.Extractor;
import com.octopus.core.processor.annotation.Link;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.core.utils.RateLimiter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 下载壁纸天堂壁纸 https://wallhaven.cc
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
@Slf4j
@Extractor(
    links = {
      @Link(cssSelector = @CssSelector(value = "#thumbs ul li img", attribute = "data-src")),
      @Link(cssSelector = @CssSelector(value = "ul.pagination li a.next", attribute = "href"))
    })
public class WallpaperCrawler {

  @CssSelector(value = "#thumbs ul li")
  private List<Wallpaper> wallpapers;

  public List<Wallpaper> getWallpapers() {
    return wallpapers;
  }

  public void setWallpapers(List<Wallpaper> wallpapers) {
    this.wallpapers = wallpapers;
  }

  @Extractor
  public static class Wallpaper {

    @CssSelector(value = "img", attribute = "data-src")
    private String src;

    @CssSelector(value = "a.preview", attribute = "href")
    private String previewSrc;

    public String getSrc() {
      return src;
    }

    public void setSrc(String src) {
      this.src = src;
    }

    public String getPreviewSrc() {
      return previewSrc;
    }

    public void setPreviewSrc(String previewSrc) {
      this.previewSrc = previewSrc;
    }
  }

  public static void main(String[] args) {
    Octopus octopus =
        Octopus.builder()
            .autoStop(true)
            .setThreads(4)
            .useOkHttpDownloader()
            .addSite(WebSite.of("wallhaven.cc").setRateLimiter(RateLimiter.of(2, 10)))
            // .addProcessor(new ListPageProcessor())
            .addProcessor(Matchers.HTML, WallpaperCrawler.class)
            .addProcessor(
                new MediaFileDownloadProcessor("/Users/yann/Downloads/wallpapers/wallhaven/nsfw"))
            .build();
    octopus.addRequest(
        Request.get(
            "https://wallhaven.cc/search?categories=110&purity=010&ratios=16x9%2C16x10&sorting=hot&order=desc&page=1"));
    octopus.start();
  }
}
