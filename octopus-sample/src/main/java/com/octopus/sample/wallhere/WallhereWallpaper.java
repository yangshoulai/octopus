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
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.extractor.selector.CssSelector;
import com.octopus.core.extractor.selector.JsonSelector;
import com.octopus.core.extractor.selector.XpathSelector;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.matcher.Matchers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
@Slf4j
@Data
@Extractor
@Links(
    @Link(
        cssSelectors =
            @CssSelector(expression = ".hub-photomodal > a", multi = false, attr = "href")))
public class WallhereWallpaper {

  @JsonSelector(expression = "$.data")
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

    @CssSelector(expression = ".item")
    private Wallpaper[] wallpapers;
  }

  @Data
  @Extractor
  @Link(xpathSelectors = @XpathSelector(expression = "//div[@class='item-container']/a[1]/@href"))
  public static class Wallpaper {

    @XpathSelector(expression = "//div[@class='item-container']/a[1]/@href")
    @RegexFormatter(format = "https://wallhere.com%s")
    private String href;
  }

  public static void main(String[] args) {
    Octopus.builder()
        .setName("wallhere-spider")
        .debug()
        .setThreads(3)
        .addSite(WebSite.of("wallhere.com").setRateLimiter(1, 3))
        .addProcessor(Matchers.or(Matchers.JSON, Matchers.HTML), WallhereWallpaper.class)
        .addProcessor(new MediaFileDownloadProcessor("../../../downloads/wallpapers/wallhere"))
        .addSeeds(
            "https://wallhere.com/zh/wallpapers?page=1&direction=horizontal&order=popular&format=json")
        .build()
        .start();
  }
}
