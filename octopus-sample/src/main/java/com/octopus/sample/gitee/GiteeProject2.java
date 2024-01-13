package com.octopus.sample.gitee;

import cn.hutool.json.JSONUtil;
import com.octopus.core.configuration.OctopusBuilderProperties;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.ConfigurableProcessor;
import com.octopus.core.processor.extractor.annotation.*;
import com.octopus.core.processor.extractor.configurable.TextProcessorProperties;
import lombok.Data;

import java.io.IOException;
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
public class GiteeProject2 {

    @Css(value = ".items .item", self = true)
    private Collection<Project> projects;

    @Data
    @Extractor
    public static class Project {

        @Css(".project-title a.title")
        private String name;

        @Css(
                value = ".project-title a.title",
                attr = "href",
                formatter = @Formatter(regex = "^.*$", format = "https://gitee.com%s"))
        private String address;

        @Css(".project-desc")
        private String description;

        @Css(".project-label-item")
        private List<String> tags;

        @Css(".stars-count")
        private int stars;

        @Url
        private String url;
    }

    public static void main(String[] args) throws IOException, ValidateException {
        OctopusBuilderProperties builder = OctopusBuilderProperties.fromYaml(GiteeProject2.class.getResourceAsStream("/gitee/octopus.yaml"));
        TextProcessorProperties processor = TextProcessorProperties.fromYaml(GiteeProject2.class.getResourceAsStream("/gitee/processor.yaml"));
        ConfigurableProcessor p = processor.toProcessor();
        p.setCollector(s -> System.out.println(JSONUtil.toJsonPrettyStr(s)));
        builder.toBuilder().addProcessor(p).build().start();
    }
}
