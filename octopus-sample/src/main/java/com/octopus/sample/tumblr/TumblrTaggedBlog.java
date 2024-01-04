package com.octopus.sample.tumblr;

import cn.hutool.core.util.URLUtil;
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
import com.octopus.core.processor.extractor.selector.*;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.sample.Constants;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 下载汤不热博主发布的图片与视频
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
@Data
@Extractor(
        links = {
                @Link(selector = @Selector(type = Selector.Type.Json, value = "$.response.timeline._links.next.href", formatter = @Formatter(format = "https://www.tumblr.com/api%s", regex = "^[\\w\\W]*$", groups = {0})), repeatable = false),
                @Link(selector = @Selector(type = Selector.Type.Json, value = "$.response.timeline.elements[*].content[*].media[0].url"), repeatable = false)
        }
)
public class TumblrTaggedBlog {
    public static void main(String[] args) {
        ProxyProvider proxyProvider = new PollingProxyProvider(Constants.PROXY);
        DownloadConfig downloadConfig = new DownloadConfig();
        downloadConfig.setProxyProvider(proxyProvider);
        downloadConfig.setSocketTimeout(120000);
        downloadConfig.setConnectTimeout(12000);
        downloadConfig.addHeader("Authorization", "Bearer aIcXSOoTtqrzR8L8YEIOmBeW94c3FmbSNSWAUbxsny9KKx5VFh");
        String tag = "dota";
        Request seed = Request.get("https://www.tumblr.com/api/v2/hubs/" + URLUtil.encode(tag) + "/timeline")
                .addParam("fields[blogs]", "name,avatar,title,url,blog_view_url,is_adult,?is_member,description_npf,uuid,can_be_followed,?followed,?advertiser_name,theme,?primary,?is_paywall_on,?paywall_access,?subscription_plan,tumblrmart_accessories,?live_now,can_show_badges,share_likes,share_following,can_subscribe,subscribed,ask,?can_submit,?is_blocked_from_primary,?is_blogless_advertiser,is_password_protected")
                .addParam("sort", "top")
                .addParam("limit", "14");
        Octopus.builder()
                .addSeeds(seed)
                .addProcessor(Matchers.JSON, TumblrTaggedBlog.class)
                .addProcessor(new MediaFileDownloadProcessor(Constants.DOWNLOAD_DIR + "/tumblr/" + tag))
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
