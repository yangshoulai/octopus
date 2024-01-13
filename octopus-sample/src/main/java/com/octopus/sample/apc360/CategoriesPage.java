package com.octopus.sample.apc360;

import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.Formatter;
import com.octopus.core.processor.extractor.annotation.Json;
import com.octopus.core.processor.extractor.annotation.Selector;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
@Data
@Extractor({
  @Link(
      selector =
          @Selector(
              type = Selector.Type.Json,
              value = "$.data[*].id",
              formatter =
                  @Formatter(
                      format =
                          "http://wallpaper.apc.360.cn/index.php?c=WallPaper&start=0&count=200&from=360chrome&a=getAppsByCategory&cid=%s")))
})
public class CategoriesPage {

  @Json("$.errno")
  private int errno;

  @Json("$.errmsg")
  private String errmsg;

  @Json("$.consume")
  private int consume;

  @Json("$.total")
  private int total;

  @Json("$.data[*]")
  private List<Category> categories;

  @Data
  @Extractor
  public static class Category {

    @Json(value = "$.id")
    private int id;

    @Json(value = "$.name")
    private String name;

    @Json("$.order_num")
    private int orderNum;

    @Json("$.tag")
    private String tag;

    @Json("$.create_time")
    private Date createTime;
  }
}
