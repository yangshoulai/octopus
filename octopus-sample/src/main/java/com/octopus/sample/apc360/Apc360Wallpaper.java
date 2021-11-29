package com.octopus.sample.apc360;

import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.matcher.Matchers;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
@Slf4j
public class Apc360Wallpaper {

  public static void main(String[] args) {

    Octopus.builder()
        .autoStop()
        .addSite(WebSite.of("p2.qhimg.com").setRateLimiter(16, 1))
        .addSeeds(
            "http://wallpaper.apc.360.cn/index.php?c=WallPaper&a=getAllCategoriesV2&from=360chrome")
        .addProcessor(Matchers.urlRegex(".*getAllCategoriesV2.*"), CategoriesPage.class)
        .addProcessor(Matchers.urlRegex(".*getAppsByCategory.*"), WallpapersPage.class)
        .addProcessor(new MediaFileDownloadProcessor("../../downloads/wallpapers/360"))
        .build()
        .start();
  }
}
