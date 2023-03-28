package com.octopus.sample.apc360;

import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher.Type;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.selector.Formatter;
import com.octopus.core.processor.extractor.selector.Selector;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
@Data
@Extractor(matcher = @ExtractorMatcher(type = Type.URL_REGEX, regex = ".*getAllCategoriesV2.*"))
@Link(
    selectors =
        @Selector(
            type = Selector.Type.Json,
            value = "$.data[*].id",
            formatters =
                @Formatter(
                    format =
                        "http://wallpaper.apc.360.cn/index.php?c=WallPaper&start=0&count=200&from=360chrome&a=getAppsByCategory&cid=%s")))
public class CategoriesPage {

  @Selector(type = Selector.Type.Json, value = "$.errno")
  private int errno;

  @Selector(type = Selector.Type.Json, value = "$.errmsg")
  private String errmsg;

  @Selector(type = Selector.Type.Json, value = "$.consume")
  private int consume;

  @Selector(type = Selector.Type.Json, value = "$.total")
  private int total;

  @Selector(type = Selector.Type.Json, value = "$.data[*]")
  private List<Category> categories;

  @Data
  @Extractor
  public static class Category {

    @Selector(type = Selector.Type.Json, value = "$.id")
    private int id;

    @Selector(type = Selector.Type.Json, value = "$.name")
    private String name;

    @Selector(type = Selector.Type.Json, value = "$.order_num")
    private int orderNum;

    @Selector(type = Selector.Type.Json, value = "$.tag")
    private String tag;

    @Selector(type = Selector.Type.Json, value = "$.create_time")
    private Date createTime;
  }
}
