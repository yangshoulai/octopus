package com.octopus.sample.bing;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.Selector;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.sample.Constants;

/**
 * 下载必应壁纸 https://bing.ioliu.cn/
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/1/15
 */
@Extractor({
  @Link(
      selector = @Selector(type = Selector.Type.Xpath, value = "//a[text()='下一页']/@href"),
      repeatable = false),
  @Link(
      selector = @Selector(type = Selector.Type.Xpath, value = "//a[@class='ctrl download']/@href"),
      repeatable = false,
      priority = 2)
})
public class BingWallpaper {

  public static void main(String[] args) {
    Octopus.builder()
        .setThreads(2)
        .addSite(
            WebSite.of("bing.ioliu.cn")
                .setRateLimiter(1, 5)
                .setDownloadConfig(new DownloadConfig()))
        .addProcessor(Matchers.HTML, BingWallpaper.class)
        .addProcessor(new MediaFileDownloadProcessor(Constants.DOWNLOAD_DIR + "/wallpapers/bing"))
        .addSeeds(Request.get("https://bing.ioliu.cn"))
        .build()
        .start();
  }
}
