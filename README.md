# Octopus

一款设计优雅、配置灵活、功能强大的分布式爬虫框架。支持注解和配置文件两种开发方式，内置丰富的内容提取和数据处理能力，并提供完善的限流、代理、重试等爬虫控制机制。

## 特性

- 多种配置方式支持

  - 注解方式 - 简单直观的配置方式
  - 配置文件方式 - 灵活的 YAML 配置
  - 代码方式 - 完全可编程的配置

- 丰富的内容提取能力

  - 支持 CSS 选择器
  - 支持 XPath 表达式
  - 支持正则表达式
  - 支持 JsonPath
  - 支持链式提取

- 强大的数据处理能力

  - 支持多种数据类型自动转换
  - 支持数据清洗和格式化
  - 支持自定义数据处理器

- 完善的爬虫控制

  - 支持目标站点限速
  - 支持代理配置
  - 支持请求重试
  - 支持断点续爬
  - 支持深度控制

- 多样化的数据存储

  - 支持内存存储
  - 支持 Redis 存储
  - 支持 MongoDB 存储
  - 支持自定义存储实现

- 异常处理机制
  - 支持请求失败重试
  - 支持异常自动恢复
  - 支持自定义异常处理

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.octopus</groupId>
    <artifactId>octopus-core</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 示例一：注解方式爬取 Gitee 项目

以下示例展示如何爬取 [Gitee](https://gitee.com/explore/all?order=starred) 推荐项目列表:

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
                                gitee.getProjects().forEach(p ->
                                    System.out.println(JSONUtil.toJsonStr(p))
                                );
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
                denoiser = @Denoiser(
                    regex = "^.*$",
                    format = "https://gitee.com%s"
                ))
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
}
```

### 示例二：配置文件方式

使用 YAML 配置文件方式同样可以实现上述爬虫:

```yaml
name: Gitee
autoStop: true
clearStoreOnStartup: true
threads: 20

seeds:
  - https://gitee.com/explore/all?order=starred

sites:
  - host: gitee.com
    limitInSecond: 1

processors:
  - matcher: Html
    extractor:
      fields:
        - name: projects
          multi: true
          selector:
            css:
              expression: .items .item
              self: true
          extractor:
            fields:
              - name: name
                selector:
                  css: .project-title a.title
              - name: address
                selector:
                  css:
                    expression: .project-title a.title
                    attr: href
                    denoiser:
                      format: https://gitee.com%s
              - name: description
                selector:
                  css: .project-desc
              - name: tags
                selector:
                  css: .project-label-item
              - name: stars
                selector:
                  css: .stars-count
                type: Integer
              - name: url
                selector:
                  url: {}
      links:
        - priority: 1
          repeatable: false
          selector:
            xpath: //a[@rel='next'][position()=2]/@href
```

```java
public class GiteeProject2 {
    public static void main(String[] args) throws Exception {
        Octopus.fromYaml(
            GiteeProject2.class.getResourceAsStream("/gitee/octopus.yaml")
        ).start();
    }
}
```

## 高级特性

### JEXL 表达式支持

Octopus 支持在配置中使用 JEXL 表达式进行灵活的数据处理:

| 函数                  | 说明         | 示例                                            |
| --------------------- | ------------ | ----------------------------------------------- |
| **env:get**           | 获取环境变量 | `env:get('user.home')`                          |
| **regex:get**         | 正则提取内容 | `regex:get('.*/([^/]+.jpg).*', request.url, 1)` |
| **encoder:urlEncode** | URL 编码     | `encoder:urlEncode(result.name, 'UTF-8')`       |
| **decoder:urlDecode** | URL 解码     | `decoder:urlDecode(result.name, 'UTF-8')`       |

### 代理支持

支持配置 HTTP、SOCKS 代理:

```yaml
sites:
  - host: example.com
    proxies:
      - type: HTTP # 或 SOCKS
        host: 127.0.0.1
        port: 7890
        username: user # 可选
        password: pass # 可选
```

### 自定义组件

Octopus 支持自定义各种组件来扩展功能:

- 自定义下载器
- 自定义存储器
- 自定义处理器
- 自定义收集器

## 更多示例

完整的示例代码请参考：[octopus-sample](https://github.com/yangshoulai/octopus/tree/master/octopus-sample)

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进 Octopus。

## 配置

| 属性          | 类型          | 必填 | 默认值                          | 说明               |
| ------------- | ------------- | ---- | ------------------------------- | ------------------ |
| **trim**      | **Boolean**   | 否   | `true`                          | 是否去除前后空格   |
| **filter**    | **Boolean**   | 否   | `true`                          | 是否过滤空内容     |
| **split**     | **Boolean**   | 否   | `false`                         | 是否分割           |
| **separator** | **String**    | 否   | ；\|;\|，\|,\|#\| \|、\|/\|\\\\ | \\                 |
| **groups**    | **Integer[]** | 否   | `[]`                            | 正则表达式匹配的组 |
| **format**    | **String**    | 否   | `%s`                            | 字符串格式化       |
| **regex**     | **String**    | 否   |                                 | 正则表达式提取     |

### <a name="Selector">Selector</a>

选择器

| 属性             | 类型                                  | 必填 | 默认值           | 说明                |
| ---------------- | ------------------------------------- | ---- | ---------------- | ------------------- |
| **denoiser**     | **[Denoiser](#Denoiser)**             | 否   | `默认降噪器配置` | 降噪器配置          |
| **defaultValue** | **String**                            | 否   |                  | 默认值              |
| **attr**         | **[AttrSelector](#AttrSelector)**     | 否   |                  | 属性选择器          |
| **body**         | **[BodySelector](#BodySelector)**     | 否   |                  | 响应体选择器        |
| **css**          | **[CssSelector](#CssSelector)**       | 否   |                  | 响应体 CSS 选择器   |
| **header**       | **[HeaderSelector](#HeaderSelector)** | 否   |                  | 响应头选择器        |
| **json**         | **[JsonSelector](#JsonSelector)**     | 否   |                  | 响应体 JSON 选择器  |
| **param**        | **[ParamSelector](#ParamSelector)**   | 否   |                  | 请求参数选择器      |
| **regex**        | **[RegexSelector](#RegexSelector)**   | 否   |                  | 响应体正则选择器    |
| **url**          | **[UrlSelector](#UrlSelector)**       | 否   |                  | 请求链接选择器      |
| **value**        | **[ValueSelector](#ValueSelector)**   | 否   |                  | 固定值选择器        |
| **xpath**        | **[XpathSelector](#XpathSelector)**   | 否   |                  | 响应体 XPATH 选择器 |
| **id**           | **[IdSelector](#IdSelector)**         | 否   |                  | 请求 ID 选择器      |
| **env**          | **[EnvSelector](#EnvSelector)**       | 否   |                  | 环境变量选择器      |
| **index**        | **[IndexSelector](#IndexSelector)**   | 否   |                  | 请求索引选择器      |
| **none**         | **[NoneSelector](#NoneSelector)**     | 否   |                  | 空选择器            |

#### <a name="AttrSelector">AttrSelector</a>

属性选择器 从请求属性中获取内容

| 属性             | 类型                      | 必填 | 默认值           | 说明       |
| ---------------- | ------------------------- | ---- | ---------------- | ---------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置 |
| **defaultValue** | **String**                | 否   |                  | 默认值     |
| **name**         | **String**                | 是   |                  | 属性名称   |

#### <a name="BodySelector">BodySelector</a>

响应体选择器 按照`UTF-8`编码

| 属性             | 类型                      | 必填 | 默认值           | 说明       |
| ---------------- | ------------------------- | ---- | ---------------- | ---------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置 |
| **defaultValue** | **String**                | 否   |                  | 默认值     |

#### <a name="CssSelector">CssSelector</a>

响应体 CSS 选择器

| 属性             | 类型                      | 必填 | 默认值           | 说明             |
| ---------------- | ------------------------- | ---- | ---------------- | ---------------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置       |
| **defaultValue** | **String**                | 否   |                  | 默认值           |
| **expression**   | **String**                | 是   |                  | CSS 查询表达式   |
| **self**         | **Boolean**               | 否   | `false`          | 是否选择节点自身 |
| **attr**         | **String**                | 否   |                  | 节点属性名称     |

#### <a name="HeaderSelector">HeaderSelector</a>

响应头选择器

| 属性             | 类型                      | 必填 | 默认值         | 说明       |
| ---------------- | ------------------------- | ---- | -------------- | ---------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | 默认降噪器配置 | 降噪器配置 |
| **defaultValue** | **String**                | 否   |                | 默认值     |
| **name**         | **String**                | 是   |                | 响应头名称 |

#### <a name="JsonSelector">JsonSelector</a>

响应体 JSON 选择器

| 属性             | 类型                      | 必填 | 默认值           | 说明            |
| ---------------- | ------------------------- | ---- | ---------------- | --------------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置      |
| **defaultValue** | **String**                | 否   |                  | 默认值          |
| **expression**   | **String**                | 是   |                  | JsonPath 表达式 |

#### <a name="XpathSelector">XpathSelector</a>

响应体 XPATH 选择器

| 属性             | 类型                      | 必填 | 默认值         | 说明         |
| ---------------- | ------------------------- | ---- | -------------- | ------------ |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | 默认降噪器配置 | 降噪器配置   |
| **defaultValue** | **String**                | 否   |                | 默认值       |
| **expression**   | **String**                | 是   |                | Xpath 表达式 |
| **node**         | **Boolean**               | 否   | `true`         | 是否节点     |

#### <a name="ParamSelector">ParamSelector</a>

请求查询参数选择器

| 属性             | 类型                      | 必填 | 默认值           | 说明         |
| ---------------- | ------------------------- | ---- | ---------------- | ------------ |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置   |
| **defaultValue** | **String**                | 否   |                  | 默认值       |
| **name**         | **String**                | 是   |                  | 查询参数名称 |

#### <a name="RegexSelector">RegexSelector</a>

请求体正则选择器

| 属性             | 类型                      | 必填 | 默认值           | 说明                               |
| ---------------- | ------------------------- | ---- | ---------------- | ---------------------------------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置                         |
| **defaultValue** | **String**                | 否   |                  | 默认值                             |
| **expression**   | **String**                | 是   |                  | 正则表达式                         |
| **groups**       | **Integer[]**             | 否   | `[0]`            | 提取的组，组的顺序即是格式化的顺序 |
| **format**       | **String**                | 否   | `%s`             | 格式化                             |

#### <a name="UrlSelector">UrlSelector</a>

请求链接选择器

| 属性             | 类型                      | 必填 | 默认值           | 说明       |
| ---------------- | ------------------------- | ---- | ---------------- | ---------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置 |
| **defaultValue** | **String**                | 否   |                  | 默认值     |

#### <a name="ValueSelector">ValueSelector</a>

固定值选择器

| 属性             | 类型                      | 必填 | 默认值           | 说明       |
| ---------------- | ------------------------- | ---- | ---------------- | ---------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置 |
| **defaultValue** | **String**                | 否   |                  | 默认值     |
| **value**        | **String**                | 否   |                  | 固定值     |

#### <a name="IdSelector">IdSelector</a>

请求 ID 选择器

| 属性             | 类型                      | 必填 | 默认值           | 说明       |
| ---------------- | ------------------------- | ---- | ---------------- | ---------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置 |
| **defaultValue** | **String**                | 否   |                  | 默认值     |

#### <a name="EnvSelector">EnvSelector</a>

环境变量选择器

| 属性             | 类型                      | 必填 | 默认值           | 说明         |
| ---------------- | ------------------------- | ---- | ---------------- | ------------ |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置   |
| **defaultValue** | **String**                | 否   |                  | 默认值       |
| **name**         | **String**                | 否   |                  | 环境变量名称 |

#### <a name="IndexSelector">IndexSelector</a>

请求索引选择器

| 属性             | 类型                      | 必填 | 默认值           | 说明       |
| ---------------- | ------------------------- | ---- | ---------------- | ---------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置 |
| **defaultValue** | **String**                | 否   |                  | 默认值     |

#### <a name="NoneSelector">NoneSelector</a>

空选择器

不选择任何内容

| 属性             | 类型                      | 必填 | 默认值           | 说明       |
| ---------------- | ------------------------- | ---- | ---------------- | ---------- |
| **denoiser**     | **[Denoiser](#Denoiser)** | 否   | `默认降噪器配置` | 降噪器配置 |
| **defaultValue** | **String**                | 否   |                  | 默认值     |

## Octopus 配置

| 属性                                | 类型                          | 必填 | 默认值     | 说明                                                               |
| ----------------------------------- | ----------------------------- | ---- | ---------- | ------------------------------------------------------------------ |
| **name**                            | **String**                    | 否   | `Octopus`  | 爬虫名称                                                           |
| **threads**                         | **Integer**                   | 否   | `2 * cpu`  | 爬虫线程数                                                         |
| **autoStop**                        | **Boolean**                   | 否   | `true`     | 下载完成是否自动关闭爬虫                                           |
| **clearStoreOnStartup**             | **Boolean**                   | 否   | `true`     | 启动时是否清空请求存储器                                           |
| **clearStoreOnStop**                | **Boolean**                   | 否   | `true`     | 停止时是否清空请求存储器                                           |
| **ignoreSeedsWhenStoreHasRequests** | **Boolean**                   | 否   | `true`     | 当请求存储器中还有未处理完的请求时是否忽略种子                     |
| **replayFailedRequest**             | **Boolean**                   | 否   | `true`     | 是否重试失败请求                                                   |
| **maxReplays**                      | **Integer**                   | 否   | `1`        | 最大重试次数                                                       |
| **maxDepth**                        | **Integer**                   | 否   | `-1`       | 最大爬取深度 , 负数代表无限制                                      |
| **sites**                           | **[Site[]](#Site)**           | 否   |            | 目标站点配置，可以配置目标站点的爬取速率，请求头，超时时间、代理等 |
| **seeds**                           | **[Seed[]](#Seed)**           | 否   |            | 种子请求配置                                                       |
| **downloader**                      | **[Downloader](#Downloader)** | 否   | `OkHttp`   | 下载器配置                                                         |
| **store**                           | **[Store](#Store)**           | 否   | `内存存储` | 存储器配置                                                         |
| **processors**                      | **[Processor[]](#Processor)** | 否   |            | 处理器配置                                                         |

### <a name="Site">Site</a>

目标站点配置

| 属性               | 类型                                  | 必填 | 默认值 | 说明                           |
| ------------------ | ------------------------------------- | ---- | ------ | ------------------------------ |
| **host**           | **String**                            | 是   |        | 站点域名                       |
| **limitInSecond**  | **Double**                            | 否   |        | 站点限速，一秒内能发送多少请求 |
| **downloadConfig** | **[DownloadConfig](#DownloadConfig)** | 否   |        | 站点下载配置                   |

#### <a name="DownloadConfig">DownloadConfig</a>

目标站点下载配置

| 属性                 | 类型                   | 必填 | 默认值  | 说明                   |
| -------------------- | ---------------------- | ---- | ------- | ---------------------- |
| **timeoutInSeconds** | **Integer**            | 否   | `60`    | 下载超时时间，单位`秒` |
| **charset**          | **String**             | 否   | `UTF-8` | 站点默认字符编码方式   |
| **headers**          | **Map<String,String>** | 否   |         | 自定义请求头           |
| **proxies**          | **[Proxy[]]()**        | 否   |         | 站点代理列表           |

### <a name="Seed">Seed</a>

种子请求

| 属性           | 类型                    | 必填 | 默认值  | 说明                          |
| -------------- | ----------------------- | ---- | ------- | ----------------------------- |
| **url**        | **String**              | 是   |         | 链接                          |
| **method**     | **String**              | 是   | `GET`   | 请求方法，支持`GET` 和 `POST` |
| **params**     | **Map<String, String>** | 否   |         | 请求查询参数                  |
| **headers**    | **Map<String, String>** | 否   |         | 请求头                        |
| **priority**   | **Integer**             | 否   | `0`     | 请求优先级                    |
| **repeatable** | **Boolean**             | 否   | `true`  | 是否可重复                    |
| **attributes** | **Map<String, String>** | 否   |         | 请求属性                      |
| **inherit**    | **Boolean**             | 否   | `false` | 是否继承父请求的属性          |
| **cache**      | **Boolean**             | 否   | `false` | 是否缓存                      |
| **body**       | **String**              | 否   |         | 请求体                        |

### <a name="Downloader">Downloader</a>

下载器配置

如果配置了站点的下载配置，则会覆盖这个下载器配置

| 属性                 | 类型                   | 必填 | 默认值   | 说明                                                                  |
| -------------------- | ---------------------- | ---- | -------- | --------------------------------------------------------------------- |
| **type**             | **String**             | 是   | `OkHttp` | 下载器类型，支持 `OkHttp` 和 `HttpClient` 以及 自定义下载器类名全路径 |
| **conf**             | **Map<String,String>** | 否   |          | 自定义下载器配置                                                      |
| **timeoutInSeconds** | **Integer**            | 否   | `60`     | 全局下载超时时间，单位`秒`                                            |
| **charset**          | **String**             | 否   | `UTF-8`  | 全局默认字符编码方式                                                  |
| **headers**          | **Map<String,String>** | 否   |          | 全局自定义请求头                                                      |
| **proxies**          | **[Proxy[]](#Proxy)**  | 否   |          | 全局站点代理列表                                                      |

### <a name="Store">Store</a>

存储器配置

存储器按照下面表格中的存储器顺序查找，如果都没有配置，则默认使用内存存储器

| 属性       | 类型                            | 必填 | 默认值 | 说明           |
| ---------- | ------------------------------- | ---- | ------ | -------------- |
| **custom** | **[CustomStore](#CustomStore)** | 否   |        | 自定义存储器   |
| **redis**  | **[RedisStore](#RedisStore)**   | 否   |        | Redis 存储器   |
| **mongo**  | **[MongoStore](#MongoStore)**   | 否   |        | MongoDB 存储器 |
| **sqlite** | **[SqliteStore](#SqliteStore)** | 否   |        | Sqlite 存储器  |
|            |                                 |      |        |                |

#### <a name="CustomStore">CustomStore</a>

自定义存储器

自定义存储器需要继承 `com.octopus.core.store.AbstractCustomStore`

| 属性      | 类型                    | 必填 | 默认值 | 说明             |
| --------- | ----------------------- | ---- | ------ | ---------------- |
| **store** | **String**              | 是   |        | 自定义全路径类名 |
| **conf**  | **Map<String, String>** | 否   |        | 自定义存储器配置 |

#### <a name="RedisStore">RedisStore</a>

Redis 存储器

| 属性       | 类型       | 必填 | 默认值                   | 说明                |
| ---------- | ---------- | ---- | ------------------------ | ------------------- |
| **uri**    | **String** | 是   | `redis://127.0.0.1:6379` | Redis 链接          |
| **prefix** | **String** | 是   | `octopus`                | 自定义 redis 键前缀 |

#### <a name="MongoStore">MongoStore</a>

MongDB 存储器

| 属性           | 类型       | 必填 | 默认值                       | 说明           |
| -------------- | ---------- | ---- | ---------------------------- | -------------- |
| **uri**        | **String** | 是   | `mongodb://127.0.0.1:27017/` | MongoDB 链接   |
| **database**   | **String** | 是   | `Octopus`                    | 数据库名称     |
| **collection** | **String** | 是   | `request`                    | 数据库集合名称 |

#### <a name="SqliteStore">SqliteStore</a>

MongDB 存储器

| 属性      | 类型       | 必填 | 默认值   | 说明                                                                                               |
| --------- | ---------- | ---- | -------- | -------------------------------------------------------------------------------------------------- |
| **db**    | **String** | 是   |          | 数据库文件地址，支持 **[Jexl](#Jexl)** 表达式<br/>`env:get('user.home') + '/Downloads/octopus.db'` |
| **table** | **String** | 是   | requests | 表名称                                                                                             |

### <a name="Processor">Processor</a>

处理器配置

自定义处理器需要继承`com.octopus.core.processor.impl.AbstractCustomProcessor`

| 属性          | 类型                                    | 必填 | 默认值 | 说明                                                               |
| ------------- | --------------------------------------- | ---- | ------ | ------------------------------------------------------------------ |
| **matcher**   | **[Matcher](#Matcher)**                 | 是   |        | 匹配器，匹配成功的才进行处理                                       |
| **custom**    | **[CustomProcessor](#CustomProcessor)** | 否   |        | 自定义处理器，如果配置了该属性，则会忽略 `extrator` 和 `collector` |
| **extractor** | **[Extractor](#Extractor)**             | 否   |        | 提取器，从响应内容中提取内容以及新的链接                           |
| **collector** | **[Collector](#Collector)**             | 否   |        | 搜集器，搜集提取器提取的内容                                       |

#### <a name="Matcher">Matcher</a>

匹配器

| 属性         | 类型                      | 必填 | 默认值 | 说明                                                                                       |
| ------------ | ------------------------- | ---- | ------ | ------------------------------------------------------------------------------------------ |
| **type**     | **String**                | 是   |        | 匹配器类型，参考下面匹配器类型列表                                                         |
| **regex**    | **String**                | 否   |        | 正则表达式，适用于 `ContentTypeRegex` `HeaderRegex` `UrlRegex` 匹配器类型                  |
| **header**   | **String**                | 否   |        | 响应头名称，适用于 `HeaderRegex` 匹配器类型                                                |
| **attr**     | **String**                | 否   |        | 属性名称，适用于 AttrRegex` 匹配器类型                                                     |
| **children** | **[Matcher[]](#Matcher)** | 否   |        | 子匹配器列表，使用于`And`，`Or`，`Not` 等组合类型的匹配器，`Not`匹配器只能含有一个子匹配器 |

<a name="匹配器类型"> **匹配器类型** </a>

| 类型                 | 说明                                                                            |     |
| -------------------- | ------------------------------------------------------------------------------- | --- |
| **UrlRegex**         | 基于 URL 的正则匹配器                                                           |     |
| **HeaderRegex**      | 基于响应头的正则匹配器                                                          |     |
| **ContentTypeRegex** | 基于响应内容格式的正则匹配器                                                    |     |
| **AttrRegex**        | 基于请求属性的正则匹配器                                                        |     |
| **All**              | 匹配所有请求                                                                    |     |
| **Json**             | JSON 类型匹配器                                                                 |     |
| **Html**             | Html 类型匹配器                                                                 |     |
| **Image**            | Image 类型匹配器                                                                |     |
| **Video**            | Video 类型匹配器                                                                |     |
| **Pdf**              | Pdf 类型匹配器                                                                  |     |
| **Word**             | Word 类型匹配器                                                                 |     |
| **Excel**            | Excel 类型匹配器                                                                |     |
| **Audio**            | Audio 类型匹配器                                                                |     |
| **OctetStream**      | 流类型匹配器                                                                    |     |
| **Media**            | 媒体类型匹配器，包含 `Image` `Video` `Audio` `Pdf` `Word` `Excel` `OctetStream` |     |
| **And**              | 且匹配器                                                                        |     |
| **Or**               | 或匹配器                                                                        |     |
| **Not**              | 非匹配器                                                                        |     |

#### <a name="CustomProcessor">CustomProcessor</a>

自定义处理器配置

自定义处理器需要继承`com.octopus.core.processor.impl.AbstractCustomProcessor`

| 属性          | 类型                    | 必填 | 默认值 | 说明                       |
| ------------- | ----------------------- | ---- | ------ | -------------------------- |
| **processor** | **String**              | 是   |        | 自定义处理器完成全路径类名 |
| **conf**      | **Map<String, String>** | 否   |        | 自定义处理器配置           |

#### <a name="Extractor">Extractor</a>

提取器配置

| 属性       | 类型                                    | 必填 | 默认值 | 说明       |
| ---------- | --------------------------------------- | ---- | ------ | ---------- |
| **links**  | **[LinkExtractor[]](#LinkExtractor)**   | 否   |        | 链接提取器 |
| **fields** | **[FieldExtractor[]](#FieldExtractor)** | 否   |        | 字段提取器 |

##### <a name="LinkExtractor">LinkExtractor</a>

提取器配置

`url` 和 `selector` 必须至少提供一个

| 属性           | 类型                            | 必填 | 默认值  | 说明                                  |
| -------------- | ------------------------------- | ---- | ------- | ------------------------------------- |
| **url**        | **String**                      | 否   |         | 固定链接                              |
| **selector**   | **[Selector](#Selector)**       | 否   |         | 链接选择器                            |
| **priority**   | **Integer**                     | 否   | `0`     | 优先级                                |
| **repeatable** | **Boolean**                     | 否   | `true`  | 是否可重复                            |
| **inherit**    | **Boolean**                     | 否   | `false` | 是否继承父类配置                      |
| **cache**      | **Boolean**                     | 否   | `false` | 是否缓存                              |
| **method**     | **String**                      | 否   | `GET`   | 请求方法，支持`GET`和`POST`           |
| **params**     | **[KVSelector[]](#KVSelector)** | 否   | `[]`    | 查询参数                              |
| **headers**    | **[KVSelector[]](#KVSelector)** | 否   | `[]`    | 请求头列表                            |
| **attrs**      | **[KVSelector[]](#KVSelector)** | 否   | `[]`    | 请求属性                              |
| **body**       | **String**                      | 否   |         | 请求体，支持 **[Jexl](#Jexl)** 表达式 |

###### <a name="KVSelector">KVSelector</a>

键值选择器

为请求参数，请求属性，请求头设置值

| 属性         | 类型                      | 必填 | 默认值 | 说明                                           |
| ------------ | ------------------------- | ---- | ------ | ---------------------------------------------- |
| **name**     | **String**                | 是   |        | 键名称，请求查询参数、请求属性或者请求头的名称 |
| **filed**    | **String**                | 否   |        | 字段名，值来源于提取内容的字段                 |
| **selector** | **[Selector](#Selector)** | 否   |        | 选择器，值来源于选择器                         |
| **value**    | **String**                | 否   |        | 自定义值，支持 **[Jexl](#Jexl)** 表达式        |

##### <a name="FieldExtractor">FieldExtractor</a>

提取字段选择器

| 属性          | 类型                        | 必填 | 默认值   | 说明                                                                                                                          |
| ------------- | --------------------------- | ---- | -------- | ----------------------------------------------------------------------------------------------------------------------------- |
| **name**      | **String**                  | 是   |          | 字段名称                                                                                                                      |
| **type**      | **String**                  | 否   | `String` | 字段类型，支持`BigDecimal` `String` `Integer` `Double` `Boolean` `Character` `CharSequence` `Float` `Long` `Date` `ByteArray` |
| **multi**     | **Boolean**                 | 否   | `false`  | 是否多选                                                                                                                      |
| **selector**  | **[Selector](#Selector)**   | 否   |          | 选择器                                                                                                                        |
| **extractor** | **[Extractor](#Extractor)** | 否   |          | 提取器                                                                                                                        |
| **converter** | **[Converter](#Converter)** | 否   |          | 特殊内容格式化转换器                                                                                                          |

###### <a name="Converter">Converter</a>

特殊内容格式化转换器

| 属性                   | 类型         | 必填 | 默认值                                             | 说明                             |
| ---------------------- | ------------ | ---- | -------------------------------------------------- | -------------------------------- |
| **ignoreError**        | **Boolean**  | 否   | `true`                                             | 转换出错时忽略异常               |
| **booleanFalseValues** | **String[]** | 否   | `{"", "0", "非", "否", "off", "no", "f", "false"}` | 布尔类型 判断为`false`的内容集合 |
| **dateFormatPattern**  | **String**   | 否   | `yyyy-MM-dd HH:mm:ss`                              | 日期类型 格式                    |
| **dateFormatTimeZone** | **String**   | 否   |                                                    | 日期类型 时区                    |
| **charset**            | **String**   | 否   | `UTF-8`                                            | 字节数组 编码方式                |

#### <a name="Collector">Collector</a>

提取内容收集器

| 属性         | 类型                                        | 必填 | 默认值 | 说明           |
| ------------ | ------------------------------------------- | ---- | ------ | -------------- |
| **logging**  | **[LoggingCollector](#LoggingCollector)**   | 否   |        | 日志搜集器     |
| **download** | **[DownloadCollector](#DownloadCollector)** | 否   |        | 下载搜集器     |
| **excel**    | **[ExcelCollector](#ExcelCollector)**       | 否   |        | Excel 搜集器   |
| **mongodb**  | **[MongodbCollector](#MongodbCollector)**   | 否   |        | Mongodb 搜集器 |
| **custom**   | **[CustomCollector](#CustomCollector)**     | 否   |        | 自定义搜集器   |

##### <a name="LoggingCollector">LoggingCollector</a>

日志搜集器

| 属性       | 类型        | 必填 | 默认值   | 说明                                                        |
| ---------- | ----------- | ---- | -------- | ----------------------------------------------------------- |
| **target** | **String**  | 否   | `Result` | 搜集目标，支持`Result`（提取结果） 和 `Body` （整个响应体） |
| **pretty** | **Boolean** | 否   | `true`   | 是否美化打印 JSON 内容                                      |

##### <a name="DownloadCollector">DownloadCollector</a>

下载搜集器

| 属性       | 类型        | 必填 | 默认值   | 说明                                                        |
| ---------- | ----------- | ---- | -------- | ----------------------------------------------------------- |
| **target** | **String**  | 否   | `Result` | 搜集目标，支持`Result`（提取结果） 和 `Body` （整个响应体） |
| **pretty** | **Boolean** | 否   | `true`   | 是否美化下载 JSON 内容                                      |
| **dir**    | **String**  | 是   |          | 下载目录，支持 **[Jexl](#Jexl)** 表达式                     |
| **name**   | **String**  | 否   |          | 下载文件名称，支持 **[Jexl](#Jexl)** 表达式                 |

##### <a name="ExcelCollector">ExcelCollector</a>

Excel 搜集器

| 属性         | 类型                              | 必填 | 默认值 | 说明                                          |
| ------------ | --------------------------------- | ---- | ------ | --------------------------------------------- |
| **file**     | **String**                        | 否   |        | Excel 文件路径，支持 **[Jexl](#Jexl)** 表达式 |
| **append**   | **Boolean**                       | 否   | `true` | 是否追加内容                                  |
| **mappings** | **[ExcelColumn[]](#ExcelColumn)** | 否   |        | Excel 列映射                                  |

##### <a name="MongodbCollector">MongodbCollector</a>

Excel 搜集器

| 属性            | 类型       | 必填 | 默认值 | 说明            |
| --------------- | ---------- | ---- | ------ | --------------- |
| **url**         | **String** | 是   |        | mongodb 链接    |
| **database**    | **String** | 是   | `true` | 数据库名称      |
| **collection**  | **String** | 是   |        | 表名称          |
| **idFieldName** | **String** | 是   |        | `ID` 列字段名称 |

###### <a name="ExcelColumn">ExcelColumn</a>

Excel 列配置

| 属性           | 类型                    | 必填 | 默认值 | 说明                              |
| -------------- | ----------------------- | ---- | ------ | --------------------------------- |
| **jsonPath**   | **String**              | 是   |        | jsonpath                          |
| **columnName** | **String**              | 是   |        | 列名称                            |
| **trans**      | **Map<String, String>** | 否   |        | 翻译                              |
| **autoSize**   | **Boolean**             | 否   | `true` | 是否自动适应宽度                  |
| **wrap**       | **Boolean**             | 否   | `true` | 是否自动换行                      |
| **width**      | **Ineger**              | 否   | `10`   | 列宽                              |
| **format**     | **String**              | 否   |        | 排版格式                          |
| **delimiter**  | **String**              | 否   | `\n`   | 列表链接符                        |
| **align**      | **String**              | 否   | `LEFT` | 对齐，支持`LEFT` `CENTER` `RIGHT` |

##### <a name="CustomCollector">CustomCollector</a>

自定义搜集器

自定义搜集器必须继承 `com.octopus.core.processor.impl.AbstractCustomProcessor`

| 属性          | 类型                    | 必填 | 默认值 | 说明                   |
| ------------- | ----------------------- | ---- | ------ | ---------------------- |
| **collector** | **String**              | 是   |        | 自定义搜集器全路径类名 |
| **conf**      | **Map<String, String>** | 否   |        | 自定义搜集器配置       |
