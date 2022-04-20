package com.octopus.sample.apc360;

import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher.Type;
import com.octopus.core.processor.extractor.convertor.DateConvertor;
import com.octopus.core.processor.extractor.format.RegexFormatter;
import com.octopus.core.processor.extractor.selector.JsonSelector;
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
    jsonSelectors = @JsonSelector(expression = "$.data[*].id"),
    formats =
        @RegexFormatter(
            format =
                "http://wallpaper.apc.360.cn/index.php?c=WallPaper&start=0&count=200&from=360chrome&a=getAppsByCategory&cid=%s"))
public class CategoriesPage {

  @JsonSelector(expression = "$.errno")
  private int errno;

  @JsonSelector(expression = "$.errmsg")
  private String errmsg;

  @JsonSelector(expression = "$.consume")
  private int consume;

  @JsonSelector(expression = "$.total")
  private int total;

  @JsonSelector(expression = "$.data[*]")
  private List<Category> categories;

  @Data
  @Extractor
  public static class Category {

    @JsonSelector(expression = "$.id")
    private int id;

    @JsonSelector(expression = "$.name")
    private String name;

    @JsonSelector(expression = "$.order_num")
    private int orderNum;

    @JsonSelector(expression = "$.tag")
    private String tag;

    @JsonSelector(expression = "$.create_time")
    @DateConvertor
    private Date createTime;
  }
}
