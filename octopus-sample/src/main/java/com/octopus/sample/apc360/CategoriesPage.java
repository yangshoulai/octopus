package com.octopus.sample.apc360;

import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.convertor.DateVal;
import com.octopus.core.extractor.format.RegexFormat;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
@Data
@Extractor
@Link(
    selector = @Selector(type = Selector.Type.JSON, expression = "$.data[*].id"),
    formats =
        @RegexFormat(
            format =
                "http://wallpaper.apc.360.cn/index.php?c=WallPaper&start=0&count=200&from=360chrome&a=getAppsByCategory&cid=%s"))
public class CategoriesPage {

  @Selector(type = Selector.Type.JSON, expression = "$.errno")
  private int errno;

  @Selector(type = Selector.Type.JSON, expression = "$.errmsg")
  private String errmsg;

  @Selector(type = Selector.Type.JSON, expression = "$.consume")
  private int consume;

  @Selector(type = Selector.Type.JSON, expression = "$.total")
  private int total;

  @Selector(type = Selector.Type.JSON, expression = "$.data[*]")
  private List<Category> categories;

  @Data
  @Extractor
  public static class Category {

    @Selector(type = Selector.Type.JSON, expression = "$.id")
    private int id;

    @Selector(type = Selector.Type.JSON, expression = "$.name")
    private String name;

    @Selector(type = Selector.Type.JSON, expression = "$.order_num")
    private int orderNum;

    @Selector(type = Selector.Type.JSON, expression = "$.tag")
    private String tag;

    @Selector(type = Selector.Type.JSON, expression = "$.create_time")
    @DateVal
    private Date createTime;
  }
}
