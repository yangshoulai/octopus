package com.octopus.sample.tumblr;

import cn.hutool.core.util.ReUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.CommonDownloadConfig;
import com.octopus.core.downloader.proxy.PollingProxyProvider;
import com.octopus.core.downloader.proxy.ProxyProvider;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.LinkMethod;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.matcher.Matchers;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.Data;

/**
 * 下载汤不热博主发布的图片与视频
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
@Data
@Extractor
public class Tumblr {

  private static final Pattern USERNAME_PATTERN =
      Pattern.compile(".*https://(.*)\\.tumblr\\.com/archive/?$");

  @Selector(type = Type.REGEX, expression = ".*\"API_TOKEN\":\"(\\w+)\".*", groups = 1)
  private String apiToken;

  @LinkMethod
  public Request getFirstPagePost(Response response) {
    String username = ReUtil.get(USERNAME_PATTERN, response.getRequest().getUrl(), 1);

    Map<String, String> params = new HashMap<>();
    params.put("npf", "true");
    params.put("reblog_info", "true");
    params.put("offset", "0");
    params.put("page_number", "1");

    return Request.get("https://api.tumblr.com/v2/blog/" + username + "/posts")
        .setParams(params)
        .addHeader("Authorization", "Bearer " + this.apiToken);
  }

  public static void main(String[] args) {
    ProxyProvider proxyProvider =
        new PollingProxyProvider(
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8002)));
    CommonDownloadConfig downloadConfig = new CommonDownloadConfig();
    downloadConfig.setProxyProvider(proxyProvider);
    downloadConfig.setSocketTimeout(120000);
    downloadConfig.setConnectTimeout(12000);

    Octopus.builder()
        .addSeeds("https://amkelss.tumblr.com/archive")
        .addProcessor(Matchers.HTML, Tumblr.class)
        .addProcessor(Matchers.JSON, PostResponse.class)
        .addProcessor(new MediaFileDownloadProcessor("../../../downloads/tumblr/amkelss"))
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
