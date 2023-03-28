package com.octopus.sample.douban;

import cn.hutool.core.date.DatePattern;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher.Type;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.selector.Formatter;
import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.extractor.type.DateType;
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
@Extractor(matcher = @ExtractorMatcher(type = Type.HTML))
@Link(
    selectors = @Selector(type = Selector.Type.Css, value = ".item div.hd > a", attr = "href"),
    repeatable = false)
@Link(
    selectors = @Selector(type = Selector.Type.Css, value = "span.next a", attr = "href"),
    repeatable = false,
    priority = 1)
public class DoubanMovie {

  /** 名称 */
  @Selector(type = Selector.Type.Xpath, value = "//h1/span[1]/text()")
  private String name;

  /** 评分 */
  @Selector(type = Selector.Type.Xpath, value = "//strong[@class='ll rating_num']/text()")
  private float score;

  /** 导演 */
  @Selector(type = Selector.Type.Xpath, value = "//a[@rel='v:directedBy']/text()")
  private List<String> directors;

  /** 编辑 */
  @Selector(
      type = Selector.Type.Xpath,
      value = "//span[text()='编剧']/../span[@class='attrs']/a/text()")
  private String[] writers;

  /** 主演 */
  @Selector(type = Selector.Type.Xpath, value = "//a[@rel='v:starring']")
  private Actor[] actors;

  /** 类型 */
  @Selector(type = Selector.Type.Xpath, value = "//span[@property='v:genre']/text()")
  private String[] type;

  /** 地区 */
  @Selector(type = Selector.Type.Xpath, value = "//span[text()='制片国家/地区:']/following::text()")
  private String locale;

  /** 语言 */
  @Selector(
      type = Selector.Type.Regex,
      value = "语言:</span>([\\S\\s]*?)<br/>",
      groups = 1,
      formatters = @Formatter(split = true))
  private String[] languages;

  /** 发布日期 */
  @Selector(
      type = Selector.Type.Xpath,
      value = "//span[@property='v:initialReleaseDate']/text()",
      formatters = @Formatter(regex = "^(\\d{4}-\\d{2}-\\d{2}).*$", groups = 1))
  @DateType(pattern = DatePattern.NORM_DATE_PATTERN)
  private Date publishedDate;

  /** 时长 */
  @Selector(type = Selector.Type.Xpath, value = "//span[@property='v:runtime']/@content")
  private int duration;

  /** imdb编号 */
  @Selector(type = Selector.Type.Xpath, value = "//span[text()='IMDb:']/following::text()")
  private String imdb;

  /** 简介 */
  @Selector(
      type = Selector.Type.Xpath,
      value = "//div[@id='link-report-intra']//span[@property='v:summary']/text()",
      formatters = @Formatter(trim = true))
  private String brief;

  @Data
  @Extractor
  public static class Actor {

    @Selector(
        type = Selector.Type.Xpath,
        value = "//a/@href",
        formatters = @Formatter(regex = "^/celebrity/(\\d+)/$", groups = 1))
    private String id;

    @Selector(type = Selector.Type.Css, value = "a")
    private String name;
  }
}
