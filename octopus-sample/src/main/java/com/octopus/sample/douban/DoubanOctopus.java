package com.octopus.sample.douban;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.processor.matcher.Matchers;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/1
 */
@Slf4j
public class DoubanOctopus {
  public static void main(String[] args) {
    List<DoubanMovie> movies = new ArrayList<>();
    Octopus.builder()
        .autoStop()
        .addSite(WebSite.of("movie.douban.com").setRateLimiter(1))
        .addSeeds("https://movie.douban.com/top250?start=0&filter=")
        // 通过注解来提取页面影片数据
        .addProcessor(
            Matchers.HTML,
            DoubanMovie.class,
            movie -> {
              // 模拟采集影片数据
              if (movie != null && StrUtil.isNotBlank(movie.getName())) {
                movies.add(movie);
              }
            })
        .build()
        .start();
    // 打印影片信息
    log.debug("豆瓣电影 Top 250");
    movies.forEach(p -> log.debug("{}", JSONUtil.toJsonStr(p)));
  }
}
