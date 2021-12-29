package com.octopus.sample.tumblr;

import cn.hutool.core.map.MapUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.proxy.PollingProxyProvider;
import com.octopus.core.downloader.proxy.ProxyProvider;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Matcher;
import com.octopus.core.extractor.annotation.Matcher.Type;
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.extractor.selector.JsonSelector;
import com.octopus.core.processor.MediaFileDownloadProcessor;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/1
 */
@Extractor(matcher = @Matcher(type = Type.JSON))
@Link(
    jsonSelectors = @JsonSelector(expression = "$.response.timeline._links.next.href"),
    formats = @RegexFormatter(format = "https://www.tumblr.com/api%s"))
@Link(
    jsonSelectors =
        @JsonSelector(
            expression =
                "$.response.timeline.elements[?(@.type == 'photo')].photos[*].original_size.url"))
@Link(
    jsonSelectors =
        @JsonSelector(expression = "$.response.timeline.elements[?(@.type == 'video')].video_url"))
public class SearchPhoto {

  public static void main(String[] args) {
    ProxyProvider proxyProvider =
        new PollingProxyProvider(
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8002)));
    DownloadConfig downloadConfig = new DownloadConfig();
    downloadConfig.setProxyProvider(proxyProvider);
    downloadConfig.setSocketTimeout(120000);
    downloadConfig.setConnectTimeout(12000);

    String keyword = "game";
    Map<String, String> params =
        MapUtil.builder("query", keyword)
            .put("limit", "20")
            .put("days", "0")
            .put("mode", "top")
            .put("timeline_type", "post")
            .put("skip_component", "related_tags,blog_search")
            .put("reblog_info", "false")
            .build();

    Request seed = Request.get("https://www.tumblr.com/api/v2/timeline/search").setParams(params);

    Octopus.builder()
        .addSeeds(seed)
        .addProcessor(SearchPhoto.class)
        .addProcessor(new MediaFileDownloadProcessor("../../../downloads/tumblr/" + keyword))
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
