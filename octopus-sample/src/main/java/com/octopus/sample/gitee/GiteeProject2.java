package com.octopus.sample.gitee;

import cn.hutool.json.JSONUtil;
import cn.hutool.setting.yaml.YamlUtil;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.configuration.OctopusBuilderProperties;
import com.octopus.core.configuration.RequestProperties;
import com.octopus.core.configuration.WebSiteProperties;
import com.octopus.core.processor.configurable.*;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.selector.Css;
import com.octopus.core.processor.extractor.selector.Formatter;
import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.extractor.selector.Url;
import com.octopus.core.processor.matcher.Matchers;
import lombok.Data;
import org.yaml.snakeyaml.DumperOptions;

import java.io.FileReader;
import java.io.FileWriter;
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

    public static void main(String[] args) throws IOException {

//        MatcherProperties matcher = new MatcherProperties(MatcherType.Html);
//        ProcessorProperties processor = new ProcessorProperties();
//        processor.setMatcher(matcher);
//
//        ExtractorProperties extractor = new ExtractorProperties();
//        processor.setExtractor(extractor);
//        SelectorProperties next = new SelectorProperties();
//        next.setType(Selector.Type.Xpath);
//        next.setValue("//a[@rel='next'][position()=2]/@href");
//        LinkProperties link = new LinkProperties();
//        link.setSelector(next);
//        link.setPriority(1);
//        link.setRepeatable(false);
//        extractor.getLinks().add(link);
//
//
//        SelectorProperties projectsSelector = new SelectorProperties();
//        projectsSelector.setType(Selector.Type.Css);
//        projectsSelector.setValue(".items .item");
//        projectsSelector.setSelf(true);
//        FieldProperties projects = new FieldProperties("projects", projectsSelector, new ExtractorProperties(), true);
//
//        projects.setSelector(projectsSelector);
//        projects.setExtractor(new ExtractorProperties());
//
//        FieldProperties name = new FieldProperties("name", new SelectorProperties(Selector.Type.Css, ".project-title a.title"));
//
//        SelectorProperties addressSelector = new SelectorProperties(Selector.Type.Css, ".project-title a.title");
//        addressSelector.setAttr("href");
//        addressSelector.setFormatter(new FormatterProperties());
//        addressSelector.getFormatter().setRegex("^.*$");
//        addressSelector.getFormatter().setFormat("https://gitee.com%s");
//        FieldProperties address = new FieldProperties("address", addressSelector);
//
//        FieldProperties tags = new FieldProperties("tags", new SelectorProperties(Selector.Type.Css, ".project-label-item"), true);
//
//        FieldProperties desc = new FieldProperties("description", new SelectorProperties(Selector.Type.Css, ".project-desc"));
//        FieldProperties count = new FieldProperties("stars", new SelectorProperties(Selector.Type.Css, ".stars-count"),FieldType.Integer);
//        FieldProperties url = new FieldProperties("url", new SelectorProperties(Selector.Type.Url));
//
//        projects.getExtractor().getFields().add(name);
//        projects.getExtractor().getFields().add(address);
//        projects.getExtractor().getFields().add(tags);
//        projects.getExtractor().getFields().add(desc);
//        projects.getExtractor().getFields().add(count);
//        projects.getExtractor().getFields().add(url);
//
//        extractor.getFields().add(projects);
//
//        System.out.println(JSONUtil.toJsonPrettyStr(processor));
//
//        DumperOptions options = new DumperOptions();
//        options.setPrettyFlow(true);
//        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//
//        OctopusBuilderProperties builder = new OctopusBuilderProperties("Gitee");
//        WebSiteProperties site = new WebSiteProperties("gitee.com");
//        site.setLimitInSeconds(1);
//        builder.getSites().add(site);
//        RequestProperties seed = new RequestProperties("https://gitee.com/explore/all?order=starred");
//        builder.getSeeds().add(seed);
//        builder.getProcessors().add(processor);
        // YamlUtil.dump(builder, new FileWriter("/Users/yangshoulai/Downloads/b.yaml"), options);
        OctopusBuilderProperties builder = YamlUtil.load(GiteeProject2.class.getResourceAsStream("/gitee.yaml"), OctopusBuilderProperties.class);
        builder.toBuilder()
                .build()
                .start();
    }
}
