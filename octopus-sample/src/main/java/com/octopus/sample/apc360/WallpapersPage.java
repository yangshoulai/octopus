package com.octopus.sample.apc360;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.LinkMethod;
import com.octopus.core.processor.extractor.annotation.Json;
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

  @Json("$.errno")
  private int errno;

  @Json("$.errmsg")
  private String errmsg;

  @Json("$.consume")
  private int consume;

  @Json("$.total")
  private int total;

  @Json("$.data[*]")
  private List<Wallpaper> wallpapers;

  @LinkMethod
  public List<String> getNextPage(Response response) {
    if (this.wallpapers != null && !this.wallpapers.isEmpty()) {
      String url = response.getRequest().getUrl();
      UrlQuery query = UrlQuery.of(url, null);
      int start =
          NumberUtil.parseInt(
              StrUtil.isNotBlank(query.get("start")) ? query.get("start").toString() : "0");
      Map<CharSequence, CharSequence> map = query.getQueryMap();
      map = MapUtil.builder(new HashMap<>(map)).put("start", String.valueOf(start + 200)).build();
      return ListUtil.toList(UrlBuilder.of(url).setQuery(UrlQuery.of(map)).build());
    }

    return null;
  }

  @Data
  @Extractor
  public static class Wallpaper {

    @Json("$.id")
    private int id;

    @Json("$.class_id")
    private int categoryId;

    @Json("$.url")
    private String url;

    @LinkMethod
    public String getUrl() {
      return url;
    }
  }
}
