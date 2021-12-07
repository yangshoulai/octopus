# octopus

一款简约但不简单的 Java 爬虫

#### 特点

* 高度灵活可配置
* 支持通过注解进行数据提取、转化并格式化
* 支持多种查询语法 `CSS` `XPATH` `REGEX` `JSON`
* 支持速率限制、代理配置
* ......

#### 使用

```java
Octopus.builder()
        // 添加百度站点配置 每2秒只能有一个请求
        .addSite(WebSite.of("www.baidu.com").setRateLimiter(1, 2))
        // 设置全局下载器配置
        .setGlobalDownloadConfig(new CommonDownloadConfig())
        // 添加种子
        .addSeeds("https://wwww.baidu.com")
        // 请求处理完成后是否关闭爬虫
        .autoStop(true)
        // 使用OkHttp下载器
        .useOkHttpDownloader()
        // 使用HttpClient下载器
        .useHttpClientDownloader()
        // 使用Redis存储请求 默认使用内存
        .useRedisStore()
        // 设置请求处理监听, AttributeInheritListener 表示后续的请求会继承父请求的属性
        .addListener(new AttributeInheritListener())
        // 处理线程数
        .setThreads(5)
        // 设置处理器 此处只是简单打印下载的html内容
        .addProcessor(new LoggerProcessor())
        .build()
        // 启动爬虫
        .start();
```

##### 获取豆瓣电影TOP250

使用注解完成页面数据的提取

```java
@Slf4j
public class DoubanOctopus {
  public static void main(String[] args) {
    List<DoubanMovie> movies = new ArrayList<>();
    Octopus.builder()
        .autoStop()
        .addSite(WebSite.of("movie.douban.com").setRateLimiter(1))
        .addSeeds("https://movie.douban.com/top250?start=0&filter=")
        // 通过注解来提取页面影片数据
        .addProcessor(
            Matchers.HTML,
            DoubanMovie.class,
            movie -> {
              // 模拟采集影片数据
              if (movie != null && StrUtil.isNotBlank(movie.getName())) {
                movies.add(movie);
              }
            })
        .build()
        .start();
    // 打印影片信息
    log.debug("豆瓣电影 Top 250");
    movies.forEach(p -> log.debug("{}", JSONUtil.toJsonStr(p)));
  }
}
```

```java
@Slf4j
@Data
@Extractor
@Link(
    cssSelectors = @CssSelector(expression = ".item div.hd > a", attr = "href"),
    repeatable = false)
@Link(
    cssSelectors = @CssSelector(expression = "span.next a", attr = "href"),
    repeatable = false,
    priority = 1)
public class DoubanMovie {

  /** 名称 */
  @XpathSelector(expression = "//h1/span[1]/text()")
  private String name;

  /** 评分 */
  @XpathSelector(expression = "//strong[@class='ll rating_num']/text()")
  private float score;

  /** 导演 */
  @XpathSelector(expression = "//a[@rel='v:directedBy']/text()")
  private String[] directors;

  /** 编辑 */
  @XpathSelector(expression = "//span[text()='编剧']/../span[@class='attrs']/a/text()")
  private String[] writers;

  /** 主演 */
  @XpathSelector(expression = "//a[@rel='v:starring']")
  private Actor[] actors;

  /** 类型 */
  @XpathSelector(expression = "//span[@property='v:genre']/text()")
  private String[] type;

  /** 地区 */
  @XpathSelector(expression = "//span[text()='制片国家/地区:']/following::text()")
  private String locale;

  /** 语言 */
  @XpathSelector(expression = "//span[text()='语言:']/following::text()")
  @SplitFormatter
  private String[] languages;

  /** 发布日期 */
  @XpathSelector(expression = "//span[@property='v:initialReleaseDate']/text()")
  @RegexFormatter(regex = "^(\\d{4}-\\d{2}-\\d{2}).*$", groups = 1)
  @DateConvertor(pattern = DatePattern.NORM_DATE_PATTERN)
  private Date publishedDate;

  /** 时长 */
  @XpathSelector(expression = "//span[@property='v:runtime']/@content")
  private int duration;

  /** imdb编号 */
  @XpathSelector(expression = "//span[text()='IMDb:']/following::text()")
  private String imdb;

  /** 简介 */
  @XpathSelector(expression = "//div[@id='link-report']//span[@property='v:summary']/text()")
  private String brief;

  @Data
  @Extractor
  public static class Actor {

    @XpathSelector(expression = "//a/@href")
    @RegexFormatter(regex = "^/celebrity/(\\d+)/$", groups = 1)
    private String id;

    @CssSelector(expression = "a")
    private String name;
  }
}

```

##### 下载壁纸天堂高清壁纸

```java
@Slf4j
public class WallhavenOctopus {
  public static void main(String[] args) {
    List<Wallpaper> wallpapers = new ArrayList<>();
    Octopus octopus =
        Octopus.builder()
            .setThreads(4)
            .useOkHttpDownloader()
            .addSite(WebSite.of("wallhaven.cc").setRateLimiter(2, 10))
            .addProcessor(
                Matchers.HTML,
                WallhavenWallpaper.class,
                wallhavenWallpaper -> {
                  if (wallhavenWallpaper.getWallpapers() != null) {
                    wallpapers.addAll(wallhavenWallpaper.getWallpapers());
                  }
                })
            .addProcessor(new MediaFileDownloadProcessor("../../../downloads/wallpapers/wallhaven"))
            .build();
    octopus.addRequest(
        Request.get(
            "https://wallhaven.cc/search?categories=110&purity=100&ratios=16x9%2C16x10&sorting=hot&order=desc&page=1"));
    octopus.start();
    wallpapers.forEach(wallpaper -> log.debug("{}", wallpaper));
  }
}
```

```java
@Slf4j
@Data
@Extractor
@Link(cssSelectors = @CssSelector(expression = "img#wallpaper", attr = "src"))
@Link(
    cssSelectors =
    @CssSelector(expression = "#thumbs .thumb-listing-page ul li a.preview", attr = "href"))
@Link(
    cssSelectors =
    @CssSelector(expression = "ul.pagination li a.next", attr = "href", multi = false))
public class WallhavenWallpaper {

  /** 壁纸列表 */
  @CssSelector(expression = "#thumbs .thumb-listing-page ul li")
  private List<Wallpaper> wallpapers;

  /** 壁纸数据 */
  @Data
  @Extractor
  public static class Wallpaper {

    /** 壁纸图片链接 */
    @CssSelector(expression = "img", attr = "data-src")
    private String src;

    /** 壁纸预览图链接 */
    @CssSelector(expression = "a.preview", attr = "href")
    private String previewSrc;

    /** 壁纸分辨率 */
    @CssSelector(expression = ".wall-res")
    private String resolution;
  }
}
```

##### 下载网易云热歌榜音乐

```java
public class Music163Octopus {

  public static void main(String[] args) {
    Octopus.builder()
        .autoStop()
        .addSeeds("https://music.163.com/discover/toplist?id=3778678")
        .addSite(WebSite.of("music.163.com").setRateLimiter(RateLimiter.of(1, 5)))
        .addListener(new AttributeInheritListener())
        // 列表页面处理器
        .addProcessor(new ListPageProcessor())
        // 歌曲下载链接提取
        .addProcessor(new PlayerUrlProcessor())
        // 歌曲保存
        .addProcessor(
            new MediaFileDownloadProcessor(FileUtil.file("../../downloads/music")) {
              @Override
              protected String resolveSaveName(Response response) {
                String suffix = FileUtil.getSuffix(response.getRequest().getUrl());
                return response.getRequest().getAttribute("name")
                    + (StrUtil.isBlank(suffix) ? ".mp3" : "." + suffix);
              }
            })
        .build()
        .start();
  }
}
```

```java
public class ListPageProcessor extends AbstractProcessor {

  public ListPageProcessor() {
    super(r -> r.getRequest().getUrl().contains("/discover/toplist"));
  }

  @Override
  public List<Request> process(Response response) {
    Elements elements = response.asDocument().select("ul.f-hide li a");
    return elements.stream()
        .map(
            el -> {
              String id = el.attr("href").replace("/song?id=", "");
              String name = el.text();
              Map<String, Object> params = new HashMap<>();
              params.put("ids", ListUtil.of(id));
              params.put("level", "standard");
              params.put("encodeType", "aac");
              params.put("csrf_token", "");
              Map<String, String> encryptParams = EncryptUtil.getEncryptParams(params);
              return Request.post(
                      UrlBuilder.create()
                          .setScheme("https")
                          .setHost(URLUtil.url(response.getRequest().getUrl()).getHost())
                          .setPath(UrlPath.of("/weapi/song/enhance/player/url/v1", null))
                          .build())
                  .setRepeatable(false)
                  .setBody(HttpUtil.toParams(encryptParams).getBytes(StandardCharsets.UTF_8))
                  .addHeader(Header.CONTENT_TYPE.getValue(), ContentType.FORM_URLENCODED.getValue())
                  .putAttribute("name", name);
            })
        .collect(Collectors.toList());
  }
}

```

```java
public class PlayerUrlProcessor extends AbstractProcessor {

  public PlayerUrlProcessor() {
    super(r -> r.getRequest().getUrl().contains("/enhance/player"));
  }

  @Override
  public List<Request> process(Response response) {
    if (StrUtil.isNotBlank(response.asText())) {
      JSONObject json = (JSONObject) response.asJson();
      JSONArray data = json.getJSONArray("data");
      if (data != null && data.size() > 0) {
        String url = data.getJSONObject(0).getStr("url");
        return StrUtil.isBlank(url) ? null : ListUtil.of(Request.get(url).setPriority(1));
      }
    }
    return null;
  }
}
```

更多的样例请参考代码：[octopus-sample](https://gitee.com/yangshoulai/octopus/tree/master/octopus-sample)



#### 注意点

1. 基于注解来提取数据已经能够应付大多数情况，特殊情况需要自定义解析内容只需要实现自己的`Processor`即可
2. 注解提取时，提取器必须使用`@Extractor`注解标记，多级数据提取时每一级的提取器都是如此
3. 页面链接提取可以有`@Link`注解完成，或者使用`@LinkMethod`注解完成（样例参考 `octopus-sample/src/main/java/com/octopus/sample/proxy/KxDaiLiOctopus.java`）
4. 提取器属性类型支持`int`、`Integer`、`double`、`Double`、`long`、`Long`、`float`、`Float`、`short`、`short`、`double`、`Double`、`String`、`Date` 以及自定义的`@Extractor`数据类型（可以通过`Formatters.registerFormatter` 静态方法来扩展其他数据类型），支持`数组`，`集合`
5. 数据在通过`Selector`选择出来以后会通过一系列的格式化器进行数据格式化（参见 `@RegexFormat` `@SplitFormat`等注解），一个属性上面最多只能有一个 `MultiLineFormatter`支持的注解用来将字符串转变成字符串数组，但支持多个单行的格式化器。格式化的时候，多行的格式化器会优先执行，单行的格式化器会按注解顺序一次执行（可以通过 `Formatters.registerFormatter`来扩展格式化器）
