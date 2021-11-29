package com.octopus.sample.douban;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.CommonDownloadConfig;
import com.octopus.core.extractor.Formatters;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import com.octopus.core.extractor.convertor.DateVal;
import com.octopus.core.extractor.format.RegexFormat;
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
@Extractor
@Link(selector = @Selector(expression = ".item div.hd > a", attr = "href"), repeatable = false)
@Link(
    selector = @Selector(expression = "span.next a", attr = "href"),
    repeatable = false,
    priority = 1)
public class DoubanMovie {

  /** 编号 */
  private long id;

  /** 名称 */
  @Selector(type = Type.XPATH, expression = "//h1/span[1]/text()")
  private String name;

  /** 评分 */
  @Selector(type = Type.XPATH, expression = "//strong[@class='ll rating_num']/text()")
  private float score;

  /** 导演 */
  @Selector(type = Type.XPATH, expression = "//a[@rel='v:directedBy']/text()")
  private String[] directors;

  /** 编辑 */
  @Selector(type = Type.XPATH, expression = "//span[text()='编剧']/../span[@class='attrs']/a/text()")
  private String[] writers;

  /** 主演 */
  // @Selector(expression = ".actor > .attrs > a", self = true)
  @Selector(type = Type.XPATH, expression = "//a[@rel='v:starring']")
  private Actor[] actors;

  /** 类型 */
  @Selector(type = Type.XPATH, expression = "//span[@property='v:genre']/text()")
  private String[] type;

  /** 地区 */
  @Selector(type = Type.XPATH, expression = "//span[text()='制片国家/地区:']/following::text()")
  private String locale;

  /** 语言 */
  @Selector(type = Type.XPATH, expression = "//span[text()='语言:']/following::text()")
  @LanguageFormat
  private String language;

  /** 发布日期 */
  @Selector(type = Type.XPATH, expression = "//span[@property='v:initialReleaseDate']/text()")
  @RegexFormat(
      regex = "^((\\d{4})-(\\d{2})-(\\d{2})).*$",
      format = "%s%s%s",
      groups = {4, 3, 2})
  @DateVal(pattern = "ddMMyyyy")
  private Date publishedDate;

  /** 时长 */
  @Selector(type = Type.XPATH, expression = "//span[@property='v:runtime']/@content")
  private int duration;

  /** imdb编号 */
  @Selector(type = Type.XPATH, expression = "//span[text()='IMDb:']/following::text()")
  private String imdb;

  /** 简介 */
  @Selector(
      type = Type.XPATH,
      expression = "//div[@id='link-report']//span[@property='v:summary']/text()")
  private String brief;

  @Data
  @Extractor
  public static class Actor {

    @Selector(type = Type.XPATH, expression = "//a/@href")
    @RegexFormat(regex = "^/celebrity/(\\d+)/$", groups = 1)
    private String id;

    // @Selector(type = Type.XPATH, expression = "//a/text()")
    @Selector(expression = "a")
    private String name;
  }

  public static void main(String[] args) {

    Formatters.registerFormatter(new LanguageFormatter());

    List<DoubanMovie> movies = new ArrayList<>();
    Octopus.builder()
        .autoStop()
        .addSite(
            WebSite.of("movie.douban.com")
                .setRateLimiter(1)
                .setDownloadConfig(
                    new CommonDownloadConfig()
                        .addHeader(
                            "Cookie",
                            "__gads=ID=f3177d346c80a0c1-2285ae5f0fc50092:T=1606267864:RT=1606267864:R:S=ALNI_Mbs5Veegf-5qyHfQlpSK2_8ujI2Ug; ll=\"108258\"; _vwo_uuid_v2=D6DDAB50BCA49A960121155CBECA30B2D|b43c9971ddb32791f594141bbc79c3e1; bid=V8cnWIHWGHk; _vwo_uuid_v2=D6DDAB50BCA49A960121155CBECA30B2D|b43c9971ddb32791f594141bbc79c3e1; __yadk_uid=x2Aa7sS6ciGlkgtVzsvPz3LwGyxJGVrp; __utmc=30149280; __utmc=223695111; dbcl2=\"142644688:JVE2S+quDdo\"; ck=pyn7; _pk_ref.100001.4cf6=[\"\",\"\",1637918286,\"https://open.weixin.qq.com/\"]; _pk_id.100001.4cf6=9cdf476898667647.1637831879.3.1637918286.1637890726.; _pk_ses.100001.4cf6=*; __utma=30149280.160088922.1600063535.1637888786.1637918293.19; __utmb=30149280.0.10.1637918293; __utmz=30149280.1637918293.19.14.utmcsr=open.weixin.qq.com|utmccn=(referral)|utmcmd=referral|utmcct=/; __utma=223695111.2025169261.1637831886.1637888786.1637918293.3; __utmb=223695111.0.10.1637918293; __utmz=223695111.1637918293.3.2.utmcsr=open.weixin.qq.com|utmccn=(referral)|utmcmd=referral|utmcct=/; push_noty_num=0; push_doumail_num=0")))
        // .addSeeds("https://movie.douban.com/top250?start=200&filter=")
        .addSeeds("https://movie.douban.com/subject/1419936/")
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
