package com.octopus.sample.apc360;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.NumberUtil;
import com.octopus.core.Response;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.LinkMethod;
import com.octopus.core.extractor.annotation.Selector;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
@Data
@Extractor
public class WallpapersPage {

  @Selector(type = Selector.Type.JSON, expression = "$.errno")
  private int errno;

  @Selector(type = Selector.Type.JSON, expression = "$.errmsg")
  private String errmsg;

  @Selector(type = Selector.Type.JSON, expression = "$.consume")
  private int consume;

  @Selector(type = Selector.Type.JSON, expression = "$.total")
  private int total;

  @Selector(type = Selector.Type.JSON, expression = "$.data[*]")
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

    @Selector(type = Selector.Type.JSON, expression = "$.id")
    private int id;

    @Selector(type = Selector.Type.JSON, expression = "$.class_id")
    private int categoryId;

    @Selector(type = Selector.Type.JSON, expression = "$.url")
    private String url;

    @LinkMethod
    public String getUrl() {
      return url;
    }
  }
}
