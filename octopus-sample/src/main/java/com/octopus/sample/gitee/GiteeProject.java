package com.octopus.sample.gitee;

import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.extractor.Extractor;
import com.octopus.core.extractor.Formatters;
import com.octopus.core.extractor.Link;
import com.octopus.core.extractor.Matcher;
import com.octopus.core.extractor.Matcher.Type;
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.extractor.selector.CssSelector;
import com.octopus.core.extractor.selector.XpathSelector;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取Gitee所有推荐项目 https://gitee.com/explore/all
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Slf4j
@Data
@Extractor(matcher = @Matcher(type = Type.HTML))
@Link(
    xpathSelectors = @XpathSelector(expression = "//a[@rel='next'][position()=2]/@href"),
    repeatable = false,
    priority = 1)
public class GiteeProject {

  @CssSelector(expression = ".items .item")
  private Collection<Project> projects;

  @Data
  @Extractor
  public static class Project {

    @CssSelector(expression = ".project-title a.title")
    private String name;

    @CssSelector(expression = ".project-title a.title", attr = "href")
    @RegexFormatter(regex = "^.*$", format = "https://gitee.com%s")
    private String address;

    @CssSelector(expression = ".project-desc")
    private String description;

    @CssSelector(expression = ".project-label-item")
    private List<String> tags;

    @CssSelector(expression = ".stars-count")
    @StarFormatter
    private int stars;
  }

  public static void main(String[] args) {
    Formatters.registerFormatter(new StarFormatterHandler());
    Octopus.builder()
        .addSite(WebSite.of("gitee.com").setRateLimiter(1))
        .addSeeds("https://gitee.com/explore/all?order=starred")
        .addProcessor(
            GiteeProject.class,
            gitee -> {
              if (gitee.getProjects() != null) {
                gitee.getProjects().forEach(p -> log.debug("{}", JSONUtil.toJsonStr(p)));
              }
            })
        .build()
        .start();
  }
}
