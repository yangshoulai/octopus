package com.octopus.sample.bing;

import cn.hutool.core.io.FileUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.CommonDownloadConfig;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.store.RedisStore;
import com.octopus.core.utils.RateLimiter;
import redis.clients.jedis.JedisPool;

/**
 * 下载必应壁纸 https://bing.ioliu.cn/
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/1/15
 */
public class WallpaperCrawler {

  public static void main(String[] args) {
    Octopus.builder()
        .setThreads(5)
        .autoStop()
        .clearStoreOnStartup()
        .setStore(new RedisStore("bing", new JedisPool()))
        .addSite(
            WebSite.of("bing.ioliu.cn")
                .setRateLimiter(RateLimiter.of(1, 2))
                .setDownloadConfig(new CommonDownloadConfig()))
        .addProcessor(new ListPageProcessor())
        .addProcessor(new DetailPageProcessor())
        .addProcessor(
            new MediaFileDownloadProcessor(FileUtil.file("/Users/yann/Downloads/wallpapers/bing")))
        .addSeeds(Request.get("https://bing.ioliu.cn/").putAttribute("firstPage", true))
        .build()
        .start();
  }
}
