package com.octopus.sample.douban;

import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import com.octopus.core.extractor.format.RegexFormat;
import com.octopus.core.extractor.format.SplitFormat;
import java.util.Date;
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
  @SplitFormat
  private String[] languages;

  /** 发布日期 */
  @Selector(type = Type.XPATH, expression = "//span[@property='v:initialReleaseDate']/text()")
  @RegexFormat(regex = "^(\\d{4}-\\d{2}-\\d{2}).*$", groups = 1)
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

    @Selector(expression = "a")
    private String name;
  }
}
