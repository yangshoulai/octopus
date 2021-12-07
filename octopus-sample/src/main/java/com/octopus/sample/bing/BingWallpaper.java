package com.octopus.sample.bing;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.CommonDownloadConfig;
import com.octopus.core.extractor.Extractor;
import com.octopus.core.extractor.Link;
import com.octopus.core.extractor.selector.XpathSelector;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.matcher.Matchers;

/**
 * 下载必应壁纸 https://bing.ioliu.cn/
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/1/15
 */
@Extractor
@Link(
    xpathSelectors = @XpathSelector(expression = "//a[text()='下一页']/@href", multi = false),
    repeatable = false)
@Link(
    xpathSelectors = @XpathSelector(expression = "//a[@class='ctrl download']/@href"),
    repeatable = false,
    priority = 2)
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
        .addProcessor(new MediaFileDownloadProcessor("../../../downloads/wallpapers/bing"))
        .addSeeds(Request.get("https://bing.ioliu.cn"))
        .build()
        .start();
  }
}
