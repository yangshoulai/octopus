package com.octopus.sample.wallhaven;

import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.sample.Constants;
import java.util.List;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/1
 */
@Data
@Extractor(
    links = {
      @Link(selectors = @Selector(value = "img#wallpaper", attr = "src")),
      @Link(
          selectors =
              @Selector(value = "#thumbs .thumb-listing-page ul li a.preview", attr = "href")),
      @Link(selectors = @Selector(value = "ul.pagination li a.next", attr = "href"))
    })
public class WallhavenWallpaper {

  /** 壁纸列表 */
  @Selector(value = "#thumbs .thumb-listing-page ul li")
  private List<Wallpaper> wallpapers;

  /** 壁纸数据 */
  @Data
  @Extractor
  public static class Wallpaper {

    /** 壁纸图片链接 */
    @Selector(value = "img", attr = "data-src")
    private String src;

    /** 壁纸预览图链接 */
    @Selector(value = "a.preview", attr = "href")
    private String previewSrc;

    /** 壁纸分辨率 */
    @Selector(value = ".wall-res")
    private String resolution;
  }

  public static void main(String[] args) {
    Octopus.builder()
        .setName("wallhaven-spider")
        .setThreads(2)
        .useOkHttpDownloader()
        .addSite(WebSite.of("wallhaven.cc").setRateLimiter(1, 2))
        .addProcessor(Matchers.HTML, WallhavenWallpaper.class)
        .addProcessor(
            new MediaFileDownloadProcessor(Constants.DOWNLOAD_DIR + "/wallpapers/wallhaven/"))
        .addSeeds(
            "https://wallhaven.cc/search?categories=010&purity=110&ratios=16x9%2C16x10&topRange=6M&sorting=toplist&order=desc&page=1")
        .build()
        .start();
  }
}
