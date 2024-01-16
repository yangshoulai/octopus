# octopus

一款简约但不简单的 Java 爬虫

#### 特点

* 支持注解方式和配置文件方式配置爬虫
* 支持 `Css` `Xpath` `Regex` `Json` 等多种查询语法
* 支持目标站点限速、代理
* 支持`Integer`, `Long`, `Double`, `BigDecimal` 等内容格式类型
* 支持`内存`,`Redis`,`Mongo`等多种存储方式
* 支持异常重试、断点重试

#### 使用样例

爬取[Gitee](https://gitee.com/explore/all?order=starred)所有推荐项目

##### 注解方式

```java
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

        @Css(".project-desc")
        private String description2;

        @Css(".project-label-item")
        private List<String> tags;

        @Css(".stars-count")
        private int stars;

        @Url
        private String url;


    }

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
}
```

更多的样例请参考代码：[octopus-sample](https://github.com/yangshoulai/octopus/tree/master/octopus-sample)

##### 配置文件方式

```yaml
name: Gitee
autoStop: true
clearStoreOnStartup: true
clearStoreOnStop: true
downloader: OKHttp
globalDownloadConfig:
  charset: UTF-8
  timeoutInSeconds: 60
ignoreSeedsWhenStoreHasRequests: true
maxReplays: 1
replayFailedRequest: true
seeds:
  - https://gitee.com/explore/all?order=starred
sites:
  - host: gitee.com
    limitInSecond: 1
store: Memory
threads: 20
processors:
  - matcher: Html
    collector: Logging
    extractor:
      fields:
        - extractor:
            fields:
              - name: name
                selector:
                  type: Css
                  value: .project-title a.title
              - name: address
                selector:
                  attr: href
                  formatter:
                    format: https://gitee.com%s
                    regex: ^.*$
                  type: Css
                  value: .project-title a.title
              - name: tags
                selector:
                  type: Css
                  value: .project-label-item
              - name: description
                selector:
                  type: Css
                  value: .project-desc
              - name: stars
                selector:
                  type: Css
                  value: .stars-count
                type: Integer
              - name: url
                selector:
                  type: Url
                type: String
          multi: true
          name: projects
          selector:
            self: true
            type: Css
            value: .items .item
      links:
        - priority: 1
          repeatable: false
          selector:
            type: Xpath
            value: //a[@rel='next'][position()=2]/@href

```

```java
public class GiteeProject2 {

    public static void main(String[] args) throws Exception {
        Octopus.fromYaml(GiteeProject2.class.getResourceAsStream("/gitee/octopus.yaml")).start();
    }
}


```

