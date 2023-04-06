# octopus

一款简约但不简单的 Java 爬虫

#### 特点

* 通过少量代码即可实现爬虫
* 高度灵活可配置
* 支持通过注解进行数据提取、转化并格式化
* 支持多种查询语法 `CSS` `XPATH` `REGEX` `JSON`
* 支持速率限制、代理配置
* ......

#### 使用

爬取[Gitee](https://gitee.com/explore/all?order=starred)所有推荐项目

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

    @Url private String url;
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
```

更多的样例请参考代码：[octopus-sample](https://gitee.com/yangshoulai/octopus/tree/master/octopus-sample)



#### 注意点

1. 基于注解来提取数据已经能够应付大多数情况，特殊情况需要自定义解析内容只需要实现`Processor`即可
2. 注解提取时，提取器必须使用`@Extractor`注解标记，多级数据提取时每一级的提取器都是如此
3. 页面链接提取可以有`@Link`注解完成，或者使用`@LinkMethod`注解完成
4. 提取器属性类型支持`int`、`Integer`、`double`、`Double`、`long`、`Long`、`float`、`Float`、`short`、`short`、`double`、`Double`、`String`、`Date` 以及自定义的`@Extractor`数据类型
5. 数据在通过`Selector`选择出来以后会通过一系列的格式化器进行数据格式化
6. 通过`TypeHandlerRegistry.getInstance().registerHandler`可以自定义类型解析
