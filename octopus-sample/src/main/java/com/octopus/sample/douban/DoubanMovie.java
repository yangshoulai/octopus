package com.octopus.sample.douban;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.extractor.annotation.DateType;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Format;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import com.octopus.core.processor.matcher.Matchers;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Slf4j
@Data
@Extractor(
    links = {
      @Link(
          selector = @Selector(expression = ".item div.hd > a", attr = "href"),
          repeatable = false),
      @Link(
          selector = @Selector(expression = "span.next a", attr = "href"),
          formats = @Format(regex = "^.*$", format = "https://movie.douban.com/top250%s"),
          repeatable = false,
          priority = 1)
    })
public class DoubanMovie {

  /** 编号 */
  private long id;

  /** 名称 */
  @Selector(type = Type.XPATH, expression = "//h1/span[1]/text()")
  private String name;

  /** 评分 */
  @Selector(type = Type.XPATH, expression = "//strong[@class='ll rating_num']")
  private float score;

  /** 导演 */
  @Selector(type = Type.XPATH, expression = "//a[@rel='v:directedBy']")
  private String[] directors;

  /** 编辑 */
  @Selector(type = Type.XPATH, expression = "//span[text()='编剧']/../span[@class='attrs']/a")
  private String[] writers;

  /** 主演 */
  @Selector(expression = ".actor > .attrs > a")
  private Actor[] actors;

  /** 类型 */
  @Selector(type = Type.XPATH, expression = "//span[@property='v:genre']")
  private String[] type;

  /** 地区 */
  private String locale;

  /** 语言 */
  private String language;

  /** 发布日期 */
  @Selector(type = Type.XPATH, expression = "//span[@property='v:initialReleaseDate']")
  @Format(regex = "^(\\d{4}-\\d{2}-\\d{2}).*$", groups = 1)
  @DateType(pattern = "yyyy-MM-dd")
  private Date publishedDate;

  /** 时长 */
  @Selector(type = Type.XPATH, expression = "//span[@property='v:runtime']/@content")
  private int duration;

  /** imdb编号 */
  private String imdb;

  /** 简介 */
  @Selector(type = Type.XPATH, expression = "//div[@id='link-report']//span[@property='v:summary']")
  private String brief;

  @Data
  @Extractor
  public static class Actor {

    @Selector(type = Type.XPATH, expression = "//a/@href")
    @Format(regex = "^/celebrity/(\\d+)/$", groups = 1)
    private String id;

    // @Selector(type = Type.XPATH, expression = "//a")
    @Selector(expression = "a", self = false)
    private String name;
  }

  public static void main(String[] args) {

    List<DoubanMovie> movies = new ArrayList<>();
    Octopus.builder()
        .autoStop()
        .addSite(WebSite.of("movie.douban.com").setRateLimiter(1))
        .addSeeds("https://movie.douban.com/top250?start=0&filter=")
        //.addSeeds("https://movie.douban.com/subject/1419936/")
        .addProcessor(
            Matchers.HTML,
            DoubanMovie.class,
            movie -> {
              if (movie != null && StrUtil.isNotBlank(movie.getName())) {
                movies.add(movie);
              }
            })
        .build()
        .start();
    log.debug("豆瓣电影 Top 250");
    movies.forEach(p -> log.debug("{}", JSONUtil.toJsonStr(p)));
  }
}
