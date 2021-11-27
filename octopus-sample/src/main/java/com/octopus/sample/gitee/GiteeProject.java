package com.octopus.sample.gitee;

import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.extractor.Formatters;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import com.octopus.core.extractor.format.RegexFormat;
import com.octopus.core.processor.matcher.Matchers;
import java.util.ArrayList;
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
@Extractor(
    links = {
      @Link(
          selector =
              @Selector(type = Type.XPATH, expression = "//a[@rel='next'][position()=2]/@href"),
          formats = {@RegexFormat(format = "https://gitee.com%s")},
          repeatable = false,
          priority = 1)
    })
public class GiteeProject {

  @Selector(expression = ".items .item")
  private Collection<Project> projects;

  @Data
  @Extractor
  public static class Project {

    @Selector(expression = ".project-title a.title")
    private String name;

    @Selector(expression = ".project-title a.title", attr = "href")
    @RegexFormat(regex = "^.*$", format = "https://gitee.com%s")
    private String address;

    @Selector(expression = ".project-desc")
    private String description;

    @Selector(expression = ".project-label-item")
    private List<String> tags;

    @Selector(expression = ".stars-count")
    @StarFormat
    private int stars;
  }

  public static void main(String[] args) {
    Formatters.registerFormatter(new StarFormatter());

    List<Project> projects = new ArrayList<>();
    Octopus.builder()
        .autoStop()
        .addSite(WebSite.of("gitee.com").setRateLimiter(1))
        .addSeeds("https://gitee.com/explore/all?order=starred")
        .addProcessor(
            Matchers.HTML,
            GiteeProject.class,
            gitee -> {
              if (gitee.getProjects() != null) {
                projects.addAll(gitee.getProjects());
              }
            })
        .build()
        .start();
    log.debug("所有推荐的项目");
    projects.forEach(p -> log.debug("{}", JSONUtil.toJsonStr(p)));
  }
}
