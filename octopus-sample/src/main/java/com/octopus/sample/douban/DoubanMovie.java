package com.octopus.sample.douban;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.processor.extractor.annotation.FieldExt;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.Css;
import com.octopus.core.processor.extractor.annotation.Formatter;
import com.octopus.core.processor.extractor.annotation.Regex;
import com.octopus.core.processor.extractor.annotation.Selector;
import com.octopus.core.processor.extractor.annotation.Xpath;
import com.octopus.core.processor.matcher.Matchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/1
 */
@Slf4j
@Data
@Extractor({
        @Link(
                selector = @Selector(type = Selector.Type.Css, value = ".item div.hd > a", attr = "href"),
                repeatable = false),
        @Link(
                selector = @Selector(type = Selector.Type.Css, value = "span.next a", attr = "href"),
                repeatable = false,
                priority = 1)
})
public class DoubanMovie {

    /**
     * 名称
     */
    @Xpath("//h1/span[1]/text()")
    private String name;

    /**
     * 评分
     */
    @Xpath("//strong[@class='ll rating_num']/text()")
    private float score;

    /**
     * 导演
     */
    @Xpath("//a[@rel='v:directedBy']/text()")
    private List<String> directors;

    /**
     * 编辑
     */
    @Xpath("//span[text()='编剧']/../span[@class='attrs']/a/text()")
    private String[] writers;

    /**
     * 主演
     */
    @Xpath("//a[@rel='v:starring']")
    private Actor[] actors;

    /**
     * 类型
     */
    @Xpath("//span[@property='v:genre']/text()")
    private String[] type;

    /**
     * 地区
     */
    @Xpath("//span[text()='制片国家/地区:']/following::text()")
    private String locale;

    /**
     * 语言
     */
    @Regex(
            expression = "语言:</span>([\\S\\s]*?)<br/>",
            groups = 1,
            formatter = @Formatter(split = true))
    private String[] languages;

    /**
     * 发布日期
     */
    @Xpath(
            expression = "//span[@property='v:initialReleaseDate']/text()",
            formatter = @Formatter(regex = "^(\\d{4}-\\d{2}-\\d{2}).*$", groups = 1))
    @FieldExt(dateFormatPattern = DatePattern.NORM_DATE_PATTERN)
    private Date publishedDate;

    /**
     * 时长
     */
    @Xpath("//span[@property='v:runtime']/@content")
    private int duration;

    /**
     * imdb编号
     */
    @Xpath("//span[text()='IMDb:']/following::text()")
    private String imdb;

    /**
     * 简介
     */
    @Xpath(
            value = "//div[@id='link-report-intra']//span[@property='v:summary']/text()",
            formatter = @Formatter(trim = true))
    private String brief;

    @Data
    @Extractor
    public static class Actor {

        @Xpath(value = "//a/@href", formatter = @Formatter(regex = "^/celebrity/(\\d+)/$", groups = 1))
        private String id;

        @Css("a")
        private String name;
    }

    public static void main(String[] args) {
        List<DoubanMovie> movies = new ArrayList<>();
        Octopus.builder()
                .addSite(WebSite.of("movie.douban.com").setRateLimiter(1, 2))
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
