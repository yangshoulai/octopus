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
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.LinkMethod;
import com.octopus.core.processor.extractor.annotation.Formatter;
import com.octopus.core.processor.extractor.annotation.Json;
import com.octopus.core.processor.extractor.annotation.Regex;
import com.octopus.core.processor.extractor.annotation.Selector;
import com.octopus.core.processor.extractor.annotation.Url;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.sample.Constants;

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
@Extractor
public class TumblrArchiveBlog {

    @Regex(expression = ".*\"API_TOKEN\":\"(\\w+)\".*", groups = 1)
    private String apiToken;

    @Url(formatter = @Formatter(regex = ".*https://(.*)\\.tumblr\\.com/archive/?$", groups = 1))
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

    @Data
    @Extractor({
            @Link(
                    selector =
                    @Selector(
                            type = Selector.Type.Json,
                            value = "$.response.posts[*].content[*].media.url")),
            @Link(
                    selector =
                    @Selector(
                            type = Selector.Type.Json,
                            value = "$.response.posts[*].content[*].media[0].url"))
    })
    public static class PostResponse {

        @Json("$.meta.status")
        private int status;

        @Json("$.response.total_posts")
        private int totalPosts;

        @Json("$.response.posts[*].content[*].media.url")
        private String[] videos;

        @Json("$.response.posts[*].content[*].media[*].url")
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
        ProxyProvider proxyProvider = new PollingProxyProvider(Constants.PROXY);
        DownloadConfig downloadConfig = new DownloadConfig();
        downloadConfig.setProxyProvider(proxyProvider);
        downloadConfig.setSocketTimeout(120000);
        downloadConfig.setConnectTimeout(120000);
        String author = "raynhoro";
        Octopus.builder()
                .addSeeds("https://" + author + ".tumblr.com/archive")
                .addProcessor(Matchers.HTML, TumblrArchiveBlog.class)
                .addProcessor(Matchers.JSON, PostResponse.class)
                .addProcessor(new MediaFileDownloadProcessor(Constants.DOWNLOAD_DIR + "/tumblr/" + author))
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
