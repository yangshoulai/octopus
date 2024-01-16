package com.octopus.sample.wallhere;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.NumberUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.proxy.PollingProxyProvider;
import com.octopus.core.processor.impl.MediaFileDownloadProcessor;
import com.octopus.core.processor.annotation.Extractor;
import com.octopus.core.processor.annotation.Link;
import com.octopus.core.processor.annotation.LinkMethod;
import com.octopus.core.processor.annotation.Css;
import com.octopus.core.processor.annotation.Formatter;
import com.octopus.core.processor.annotation.Json;
import com.octopus.core.processor.annotation.Selector;
import com.octopus.core.processor.annotation.Selector.Type;
import com.octopus.core.processor.annotation.Xpath;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.sample.Constants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
@Data
@Extractor(@Link(selector = @Selector(value = ".hub-photomodal > a", attr = "href")))
public class WallhereWallPaper {

  @Json("$.data")
  private Wallpapers wallpapers;

  @LinkMethod
  public List<Request> getNextPage(Response response) {
    if (this.wallpapers != null
        && this.wallpapers.getWallpapers() != null
        && this.wallpapers.getWallpapers().length > 0) {
      String url = response.getRequest().getUrl();
      UrlQuery query = UrlQuery.of(url, null);
      int page = NumberUtil.parseInt(query.get("page").toString());
      Map<CharSequence, CharSequence> map = new HashMap<>(query.getQueryMap());
      map.put("page", String.valueOf(++page));
      return ListUtil.toList(Request.get(UrlBuilder.of(url).setQuery(UrlQuery.of(map)).build()));
    }
    return null;
  }

  @Data
  @Extractor
  public static class Wallpapers {

    @Css(expression = ".item", self = true)
    private Wallpaper[] wallpapers;
  }

  @Data
  @Extractor({
    @Link(
        selector =
            @Selector(type = Type.Xpath, value = "//div[@class='item-container']/a[1]/@href"))
  })
  public static class Wallpaper {

    @Xpath(
        expression = "//div[@class='item-container']/a[1]/@href",
        formatter = @Formatter(format = "https://wallhere.com%s"))
    private String href;
  }

  public static void main(String[] args) {
    DownloadConfig downloadConfig = new DownloadConfig();
    downloadConfig.setProxyProvider(new PollingProxyProvider(Constants.PROXY));
    Octopus.builder()
        .setName("Wallhere")
        .setThreads(3)
        .setGlobalDownloadConfig(downloadConfig)
        .addSite(WebSite.of("wallhere.com").setRateLimiter(1, 2))
        .addSite(WebSite.of("get.wallhere.com").setRateLimiter(1, 2))
        .addProcessor(Matchers.or(Matchers.JSON, Matchers.HTML), WallhereWallPaper.class)
        .addProcessor(
            new MediaFileDownloadProcessor(Constants.DOWNLOAD_DIR + "/wallpapers/wallhere"))
        .addSeeds(
            "https://wallhere.com/zh/wallpapers?page=1&direction=horizontal&NSFW=on&order=latest&format=json")
        .build()
        .start();
  }
}
