package com.octopus.sample.gitee;

import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.processor.annotation.*;
import com.octopus.core.processor.matcher.Matchers;
import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * 获取Gitee所有推荐项目 https://gitee.com/explore/all
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Data
@Extractor(
        @Link(
                selector =
                @Selector(type = Selector.Type.Xpath, value = "//a[@rel='next'][position()=2]/@href"),
                repeatable = false,
                priority = 1))
public class GiteeProject {

    @Css(value = ".items .item", self = true)
    private Collection<Project> projects;

    @Body
    private byte[] body;

    public static void main(String[] args) {
        Octopus.builder()
                .addSite(WebSite.of("gitee.com").setRateLimiter(1))
                .addSeeds("https://gitee.com/explore/all?order=starred")
                .addProcessor(
                        Matchers.HTML,
                        GiteeProject.class,
                        (gitee, r) -> {
                            if (gitee.getProjects() != null) {
                                gitee.getProjects().forEach(p -> System.out.println(JSONUtil.toJsonStr(p)));
                            }
                        })
                .build()
                .start();
    }

    @Data
    @Extractor
    public static class Project {

        @Css(".project-title a.title")
        private String name;

        @Css(value = ".project-title a.title",
                attr = "href",
                denoiser = @Denoiser(regex = "^.*$", format = "https://gitee.com%s"))
        private String address;

        @Css(".project-desc")
        private String description;

        @Css(".project-desc")
        private String description2;

        @Css(".project-label-item")
        private List<String> tags;

        @Css(".stars-count")
        private int stars;

        @Url
        private String url;


    }
}
