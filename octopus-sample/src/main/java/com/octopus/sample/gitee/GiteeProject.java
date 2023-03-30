package com.octopus.sample.gitee;

import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.selector.Formatter;
import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.matcher.Matchers;
import java.util.Collection;
import java.util.List;
import lombok.Data;

/**
 * 获取Gitee所有推荐项目 https://gitee.com/explore/all
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Data
@Extractor(
    links =
        @Link(
            selectors =
                @Selector(
                    type = Selector.Type.Xpath,
                    value = "//a[@rel='next'][position()=2]/@href"),
            repeatable = false,
            priority = 1))
public class GiteeProject {

  @Selector(type = Selector.Type.Css, value = ".items .item", self = true)
  private Collection<Project> projects;

  @Data
  @Extractor
  public static class Project {

    @Selector(type = Selector.Type.Css, value = ".project-title a.title")
    private String name;

    @Selector(
        type = Selector.Type.Css,
        value = ".project-title a.title",
        attr = "href",
        formatters = @Formatter(regex = "^.*$", format = "https://gitee.com%s"))
    private String address;

    @Selector(type = Selector.Type.Css, value = ".project-desc")
    private String description;

    @Selector(type = Selector.Type.Css, value = ".project-label-item")
    private List<String> tags;

    @Selector(type = Selector.Type.Css, value = ".stars-count")
    private int stars;
  }

  public static void main(String[] args) {
    Octopus.builder()
        .addSite(WebSite.of("gitee.com").setRateLimiter(1))
        .addSeeds("https://gitee.com/explore/all?order=starred")
        .addProcessor(
            Matchers.HTML,
            GiteeProject.class,
            gitee -> {
              if (gitee.getProjects() != null) {
                gitee.getProjects().forEach(p -> System.out.println(JSONUtil.toJsonStr(p)));
              }
            })
        .build()
        .start();
  }
}
