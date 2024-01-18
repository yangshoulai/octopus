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
```

更多的样例请参考代码：[octopus-sample](https://github.com/yangshoulai/octopus/tree/master/octopus-sample)

##### 配置文件方式

```yaml
# 爬虫名称
name: Gitee
# 是否自动关闭
autoStop: true
# 启动时是否清空请求存储器
clearStoreOnStartup: true
# 关闭时是否清空请求存储器
clearStoreOnStop: true
# 当请求存储器还有未完成的请求时，是否忽略种子请求
ignoreSeedsWhenStoreHasRequests: true
# 最大重试次数
maxReplays: 1
# 是否重试失败请求
replayFailedRequest: true
# 工作线程数量
threads: 20
# 种子请求
seeds:
  - https://gitee.com/explore/all?order=starred
# 目标站点配置
sites:
  # 站点域名
  - host: gitee.com
    # 站点限速
    limitInSecond: 1
# 请求存储器
store:
  # 使用 sqlite 存储
  sqlite:
    # 数据库文件路径
    db: /Users/yangshoulai/Downloads/octopus.db
    # 表名
    table: gitee
# 下载器配置
downloader:
  # 使用 OkHttp 下载器
  type: OkHttp
  # 默认编码
  charset: UTF-8
  # 下载超时时间
  timeoutInSeconds: 60
# 处理器列表
processors:
  # 匹配器
  - matcher: Html
    # 收集器
    collector:
      # 打印日志
      logging: { }
      # 下载内容
      downloader:
        # 下载目录
        dirs:
          - value: /Users/yangshoulai/Downloads/gitee
        # 下载文件名
        name:
          param:
            name: page
            defaultValue: 1
            denoiser:
              regex: ^.*$
              format: "%s.json"
    # 内容提取（项目列表）
    extractor:
      # 字段配置列表
      fields:
        # 字段名
        - name: projects
          # 是否多个
          multi: true
          # 选择器
          selector:
            # 使用 css选择器
            css:
              # 选择符
              expression: .items .item
              # 选取节点本身
              self: true
          # 嵌套提取内容（项目）
          extractor:
            fields:
              # 项目名称
              - name: name
                selector:
                  css: .project-title a.title
                # 项目地址
              - name: address
                selector:
                  css:
                    expression: .project-title a.title
                    attr: href
                    denoiser:
                      format: https://gitee.com%s
                      regex: ^.*$
                # 项目标签
              - name: tags
                selector:
                  css: .project-label-item
                # 项目描述
              - name: description
                selector:
                  css: .project-desc
                # 项目关注数
              - name: stars
                selector:
                  css: .stars-count
                type: Integer
                # 当前请求地址
              - name: url
                selector:
                  url: { }
      # 提取链接
      links:
        # 链接优先级
        - priority: 1
          # 不可重复
          repeatable: false
          # 链接选择器
          selector:
            # xpath 选择器 选取下一页
            xpath: //a[@rel='next'][position()=2]/@href

```

```java
public class GiteeProject2 {

    public static void main(String[] args) throws Exception {
        Octopus.fromYaml(GiteeProject2.class.getResourceAsStream("/gitee/octopus.yaml")).start();
    }
}


```

