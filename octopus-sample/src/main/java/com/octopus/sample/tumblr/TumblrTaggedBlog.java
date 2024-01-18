package com.octopus.sample.tumblr;

import cn.hutool.core.util.URLUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.proxy.PollingProxyProvider;
import com.octopus.core.downloader.proxy.ProxyProvider;
import com.octopus.core.processor.impl.MediaFileDownloadProcessor;
import com.octopus.core.processor.annotation.*;
import com.octopus.core.processor.annotation.Denoiser;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.sample.Constants;
import lombok.Data;

import java.util.*;

/**
 * 下载汤不热博主发布的图片与视频
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
@Data
@Extractor(
        links = {
                @Link(selector = @Selector(type = Selector.Type.Json, value = "$.response.timeline._links.next.href", denoiser = @Denoiser(format = "https://www.tumblr.com/api%s", regex = "^[\\w\\W]*$", groups = {0})), repeatable = false),
                @Link(selector = @Selector(type = Selector.Type.Json, value = "$.response.timeline.elements[*].content[*].media[0].url"), repeatable = false)
        }
)
public class TumblrTaggedBlog {
    public static void main(String[] args) {
        ProxyProvider proxyProvider = new PollingProxyProvider(Constants.PROXY);
        DownloadConfig downloadConfig = new DownloadConfig();
        downloadConfig.setProxyProvider(proxyProvider);
        downloadConfig.setSocketTimeout(120000);
        downloadConfig.setConnectTimeout(120000);
        downloadConfig.addHeader("Authorization", "Bearer aIcXSOoTtqrzR8L8YEIOmBeW94c3FmbSNSWAUbxsny9KKx5VFh");
        String[] tags = new String[]{
                "dota2",
                "dota"
        };
        List<Request> seeds = new ArrayList<>();
        for (String tag : tags) {
            Request seed1 = Request.get("https://www.tumblr.com/api/v2/hubs/" + URLUtil.encode(tag) + "/timeline")
                    .addParam("fields[blogs]", "name,avatar,title,url,blog_view_url,is_adult,?is_member,description_npf,uuid,can_be_followed,?followed,?advertiser_name,theme,?primary,?is_paywall_on,?paywall_access,?subscription_plan,tumblrmart_accessories,?live_now,can_show_badges,share_likes,share_following,can_subscribe,subscribed,ask,?can_submit,?is_blocked_from_primary,?is_blogless_advertiser,is_password_protected")
                    .addParam("sort", "top")
                    .addParam("limit", "14");
            Request seed2 = Request.get("https://www.tumblr.com/api/v2/timeline/search")
                    .addParam("fields[blogs]", "name,avatar,title,url,blog_view_url,is_adult,?is_member,description_npf,uuid,can_be_followed,?followed,?advertiser_name,theme,?primary,?is_paywall_on,?paywall_access,?subscription_plan,tumblrmart_accessories,?live_now,can_show_badges,share_following,share_likes,ask")
                    .addParam("sort", "top")
                    .addParam("limit", "20")
                    .addParam("days", "0")
                    .addParam("query", URLUtil.encode(tag))
                    .addParam("mode", "top")
                    .addParam("timeline_type", "post")
                    .addParam("skip_component", "related_tags,blog_search")
                    .addParam("reblog_info", "true");
            seeds.add(seed1);
            seeds.add(seed2);
        }
        Octopus.builder()
                .useSqliteStore(Constants.DOWNLOAD_DIR + "/octopus.db", "tumblr_dota")
                .clearStoreOnStop(false)
                .clearStoreOnStartup(true)
                .ignoreSeedsWhenStoreHasRequests()
                .addSeeds(seeds.toArray(new Request[0]))
                .addProcessor(Matchers.JSON, TumblrTaggedBlog.class)
                .addProcessor(new MediaFileDownloadProcessor(Constants.DOWNLOAD_DIR + "/tumblr/dota"))
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
