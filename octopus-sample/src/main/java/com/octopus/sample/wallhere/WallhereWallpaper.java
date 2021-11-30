package com.octopus.sample.wallhere;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.NumberUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.WebSite;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.LinkMethod;
import com.octopus.core.extractor.annotation.Links;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.format.RegexFormat;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.matcher.Matchers;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
@Slf4j
@Data
@Extractor
@Links(
    @Link(selector = @Selector(expression = ".hub-photomodal > a", multi = false, attr = "href")))
public class WallhereWallpaper {

  @Selector(type = Selector.Type.JSON, expression = "$.data")
  private WallpaperXml xml;

  @LinkMethod
  public List<Request> getNextPage(Response response) {
    if (this.xml != null
        && this.xml.getWallpapers() != null
        && this.xml.getWallpapers().length > 0) {
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
  public static class WallpaperXml {

    @Selector(expression = ".item")
    private Wallpaper[] wallpapers;
  }

  @Data
  @Extractor
  @Link(
      selector =
          @Selector(
              type = Selector.Type.XPATH,
              expression = "//div[@class='item-container']/a[1]/@href"))
  @Link(
      selector =
          @Selector(
              type = Selector.Type.XPATH,
              expression = "//div[@class='item-container']/a[1]/@href"))
  public static class Wallpaper {

    @Selector(type = Selector.Type.XPATH, expression = "//div[@class='item-container']/a[1]/@href")
    @RegexFormat(format = "https://wallhere.com%s")
    private String href;
  }

  public static void main(String[] args) {
    Octopus.builder()
        .setThreads(3)
        .addSite(WebSite.of("wallhere.com").setRateLimiter(1, 2))
        .addProcessor(
            Matchers.or(Matchers.JSON, Matchers.HTML),
            WallhereWallpaper.class,
            wallhereWallpaper -> {
              log.debug("{}", wallhereWallpaper);
            })
        .addProcessor(new MediaFileDownloadProcessor("../../../downloads/wallpapers/wallhere"))
        .addSeeds(
            "https://wallhere.com/zh/wallpapers?page=1&direction=horizontal&order=popular&format=json")
        .build()
        .start();
  }
}
