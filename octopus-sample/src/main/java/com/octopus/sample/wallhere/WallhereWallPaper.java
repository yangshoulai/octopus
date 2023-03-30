package com.octopus.sample.wallhere;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.NumberUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.WebSite;
import com.octopus.core.processor.MediaFileDownloadProcessor;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.LinkMethod;
import com.octopus.core.processor.extractor.selector.Formatter;
import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.extractor.selector.Selector.Type;
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
@Extractor(links = @Link(selectors = @Selector(value = ".hub-photomodal > a", attr = "href")))
public class WallhereWallPaper {

  @Selector(type = Type.Json, value = "$.data")
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

    @Selector(value = ".item")
    private Wallpaper[] wallpapers;
  }

  @Data
  @Extractor(
      links = {
        @Link(
            selectors =
                @Selector(type = Type.Xpath, value = "//div[@class='item-container']/a[1]/@href"))
      })
  public static class Wallpaper {

    @Selector(
        type = Type.Xpath,
        value = "//div[@class='item-container']/a[1]/@href",
        formatters = @Formatter(format = "https://wallhere.com%s"))
    private String href;
  }

  public static void main(String[] args) {
    Octopus.builder()
        .setName("wallhere-spider")
        .setThreads(3)
        .addSite(WebSite.of("wallhere.com").setRateLimiter(1, 3))
        .addProcessor(Matchers.JSON, WallhereWallPaper.class)
        .addProcessor(
            new MediaFileDownloadProcessor(Constants.DOWNLOAD_DIR + "/wallpapers/wallhere"))
        .addSeeds(
            "https://wallhere.com/zh/wallpapers?page=1&direction=horizontal&order=popular&format=json")
        .build()
        .start();
  }
}
