package com.octopus.sample.douban;

import cn.hutool.core.date.DatePattern;
import com.octopus.core.extractor.Extractor;
import com.octopus.core.extractor.Link;
import com.octopus.core.extractor.Matcher;
import com.octopus.core.extractor.Matcher.Type;
import com.octopus.core.extractor.convertor.DateConvertor;
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.extractor.format.SplitFormatter;
import com.octopus.core.extractor.selector.CssSelector;
import com.octopus.core.extractor.selector.XpathSelector;
import java.util.Date;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Slf4j
@Data
@Extractor(matcher = @Matcher(type = Type.HTML))
@Link(
    cssSelectors = @CssSelector(expression = ".item div.hd > a", attr = "href"),
    repeatable = false)
@Link(
    cssSelectors = @CssSelector(expression = "span.next a", attr = "href"),
    repeatable = false,
    priority = 1)
public class DoubanMovie {

  /** 名称 */
  @XpathSelector(expression = "//h1/span[1]/text()")
  private String name;

  /** 评分 */
  @XpathSelector(expression = "//strong[@class='ll rating_num']/text()")
  private float score;

  /** 导演 */
  @XpathSelector(expression = "//a[@rel='v:directedBy']/text()")
  private String[] directors;

  /** 编辑 */
  @XpathSelector(expression = "//span[text()='编剧']/../span[@class='attrs']/a/text()")
  private String[] writers;

  /** 主演 */
  @XpathSelector(expression = "//a[@rel='v:starring']")
  private Actor[] actors;

  /** 类型 */
  @XpathSelector(expression = "//span[@property='v:genre']/text()")
  private String[] type;

  /** 地区 */
  @XpathSelector(expression = "//span[text()='制片国家/地区:']/following::text()")
  private String locale;

  /** 语言 */
  @XpathSelector(expression = "//span[text()='语言:']/following::text()")
  @SplitFormatter
  private String[] languages;

  /** 发布日期 */
  @XpathSelector(expression = "//span[@property='v:initialReleaseDate']/text()")
  @RegexFormatter(regex = "^(\\d{4}-\\d{2}-\\d{2}).*$", groups = 1)
  @DateConvertor(pattern = DatePattern.NORM_DATE_PATTERN)
  private Date publishedDate;

  /** 时长 */
  @XpathSelector(expression = "//span[@property='v:runtime']/@content")
  private int duration;

  /** imdb编号 */
  @XpathSelector(expression = "//span[text()='IMDb:']/following::text()")
  private String imdb;

  /** 简介 */
  @XpathSelector(expression = "//div[@id='link-report']//span[@property='v:summary']/text()")
  private String brief;

  @Data
  @Extractor
  public static class Actor {

    @XpathSelector(expression = "//a/@href")
    @RegexFormatter(regex = "^/celebrity/(\\d+)/$", groups = 1)
    private String id;

    @CssSelector(expression = "a")
    private String name;
  }
}
