package com.octopus.sample.tumblr;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.proxy.PollingProxyProvider;
import com.octopus.core.downloader.proxy.ProxyProvider;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher.Type;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.LinkMethod;
import com.octopus.core.processor.extractor.selector.Formatter;
import com.octopus.core.processor.extractor.selector.Selector;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 下载汤不热博主发布的图片与视频
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
@Data
@Extractor(matcher = @ExtractorMatcher(type = Type.HTML))
public class BlogPhoto {

  @Selector(type = Selector.Type.Regex, value = ".*\"API_TOKEN\":\"(\\w+)\".*", groups = 1)
  private String apiToken;

  @Selector(
      type = Selector.Type.Url,
      formatters = @Formatter(regex = ".*https://(.*)\\.tumblr\\.com/archive/?$", groups = 1))
  private String username;

  @LinkMethod
  public Request getFirstPagePost(Response response) {
    Map<String, String> params = new HashMap<>();
    params.put("npf", "true");
    params.put("reblog_info", "true");
    params.put("offset", "0");
    params.put("page_number", "1");

    return Request.get("https://api.tumblr.com/v2/blog/" + username + "/posts")
        .setParams(params)
        .addHeader("Authorization", "Bearer " + this.apiToken);
  }

  /**
   * @author shoulai.yang@gmail.com
   * @date 2021/11/30
   */
  @Data
  @Extractor(matcher = @ExtractorMatcher(type = Type.JSON))
  @Link(selectors = @Selector(type = Selector.Type.Json, value = "$.response.posts[*].content[*].media.url"))
  @Link(selectors = @Selector(type = Selector.Type.Json, value = "$.response.posts[*].content[*].media[0].url"))
  public static class PostResponse {

    @Selector(type = Selector.Type.Json, value = "$.meta.status")
    private int status;

    @Selector(type = Selector.Type.Json, value = "$.response.total_posts")
    private int totalPosts;

    @Selector(type = Selector.Type.Json, value = "$.response.posts[*].content[*].media.url")
    private String[] videos;

    @Selector(type = Selector.Type.Json, value = "$.response.posts[*].content[*].media[*].url")
    private String[] photos;

    @LinkMethod
    public Request getNextPagePosts(Response response) {
      int pageNumber = Integer.parseInt(response.getRequest().getParams().get("page_number"));
      int offset = Integer.parseInt(response.getRequest().getParams().get("offset"));
      if (offset < this.totalPosts) {
        Map<String, String> params = new HashMap<>(response.getRequest().getParams());
        params.put("offset", String.valueOf(offset += 20));
        params.put("page_number", String.valueOf(++pageNumber));
        return Request.get(response.getRequest().getUrl())
            .setParams(params)
            .addHeaders(response.getRequest().getHeaders());
      }
      return null;
    }
  }

  public static void main(String[] args) {
    ProxyProvider proxyProvider =
        new PollingProxyProvider(
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8002)));
    DownloadConfig downloadConfig = new DownloadConfig();
    downloadConfig.setProxyProvider(proxyProvider);
    downloadConfig.setSocketTimeout(120000);
    downloadConfig.setConnectTimeout(12000);

    Octopus.builder()
        .addSeeds("https://2djp.tumblr.com/archive")
        .addProcessor(BlogPhoto.class)
        .addProcessor(PostResponse.class)
        .addProcessor(new MediaFileDownloadProcessor("../../../downloads/tumblr/2djp"))
        .setGlobalDownloadConfig(downloadConfig)
        .addSite(WebSite.of("api.tumblr.com").setRateLimiter(1))
        .addSite(WebSite.of("64.media.tumblr.com").setRateLimiter(1))
        .addSite(WebSite.of("ve.media.tumblr.com").setRateLimiter(1))
        .addSite(WebSite.of("vt.media.tumblr.com").setRateLimiter(1))
        .addSite(WebSite.of("va.media.tumblr.com").setRateLimiter(1))
        .build()
        .start();
  }
}
