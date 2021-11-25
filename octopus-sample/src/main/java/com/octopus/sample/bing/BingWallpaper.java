package com.octopus.sample.bing;

import cn.hutool.core.io.FileUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.CommonDownloadConfig;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Format;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.matcher.Matchers;

/**
 * 下载必应壁纸 https://bing.ioliu.cn/
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/1/15
 */
@Extractor(
    links = {
      @Link(
          selector =
              @Selector(
                  type = Type.XPATH,
                  expression = "//div[@class='container']//a[@class='mark']/@href"),
          formats = @Format(regex = "^.*$", format = "https://bing.ioliu.cn%s"),
          repeatable = false,
          priority = 1),
      @Link(
          selector =
              @Selector(type = Type.XPATH, expression = "//a[text()='下一页']/@href", multi = false),
          formats = @Format(regex = "^.*$", format = "https://bing.ioliu.cn%s"),
          repeatable = false),
      @Link(
          selector =
              @Selector(
                  type = Type.XPATH,
                  expression = "//a[@class='ctrl download']",
                  attr = "href",
                  multi = false),
          formats = @Format(regex = "^.*$", format = "https://bing.ioliu.cn%s"),
          repeatable = false,
          priority = 2)
    })
public class BingWallpaper {

  public static void main(String[] args) {
    Octopus.builder()
        .setThreads(2)
        .autoStop()
        .addSite(
            WebSite.of("bing.ioliu.cn")
                .setRateLimiter(1, 5)
                .setDownloadConfig(new CommonDownloadConfig()))
        .addProcessor(Matchers.HTML, BingWallpaper.class)
        .addProcessor(
            new MediaFileDownloadProcessor(FileUtil.file("/Users/yann/Downloads/wallpapers/bing")))
        .addSeeds(Request.get("https://bing.ioliu.cn"))
        .build()
        .start();
  }
}
