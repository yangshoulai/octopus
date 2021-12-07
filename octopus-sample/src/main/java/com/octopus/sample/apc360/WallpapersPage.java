package com.octopus.sample.apc360;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.NumberUtil;
import com.octopus.core.Response;
import com.octopus.core.extractor.Extractor;
import com.octopus.core.extractor.LinkMethod;
import com.octopus.core.extractor.selector.JsonSelector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
@Data
@Extractor
public class WallpapersPage {

  @JsonSelector(expression = "$.errno")
  private int errno;

  @JsonSelector(expression = "$.errmsg")
  private String errmsg;

  @JsonSelector(expression = "$.consume")
  private int consume;

  @JsonSelector(expression = "$.total")
  private int total;

  @JsonSelector(expression = "$.data[*]")
  private List<Wallpaper> wallpapers;

  @LinkMethod
  public List<String> getNextPage(Response response) {
    if (this.wallpapers != null && !this.wallpapers.isEmpty()) {
      String url = response.getRequest().getUrl();
      UrlQuery query = UrlQuery.of(url, null);
      int start = NumberUtil.parseInt(query.get("start").toString());
      Map<CharSequence, CharSequence> map = query.getQueryMap();
      map = MapUtil.builder(new HashMap<>(map)).put("start", String.valueOf(start + 200)).build();
      return ListUtil.toList(UrlBuilder.of(url).setQuery(UrlQuery.of(map)).build());
    }

    return null;
  }

  @Data
  @Extractor
  public static class Wallpaper {

    @JsonSelector(expression = "$.id")
    private int id;

    @JsonSelector(expression = "$.class_id")
    private int categoryId;

    @JsonSelector(expression = "$.url")
    private String url;

    @LinkMethod
    public String getUrl() {
      return url;
    }
  }
}
