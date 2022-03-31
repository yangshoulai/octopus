package com.octopus.sample.area;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.octopus.core.Octopus;
import com.octopus.core.WebSite;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Matcher;
import com.octopus.core.extractor.annotation.Matcher.Type;
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.extractor.selector.XpathSelector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/3/30
 */
@Slf4j
@Data
@Extractor(matcher = @Matcher(type = Type.HTML))
public class AreaPage {

  @XpathSelector(expression = "//tr[contains(@class, 'tr')]")
  @RegexFormatter(format = "<table>%s</table>")
  private List<Area> areas;

  public static void main(String[] args) {

    List<Area> roots = new ArrayList<>();

    List<Area> areas = new ArrayList<>();

    Map<String, Area> areaMap = new HashMap<>();

    Octopus.builder()
        .addSeeds("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2021/32.html")
        .addProcessor(
            AreaPage.class,
            areaPage -> {
              if (areaPage.getAreas() != null) {
                areas.addAll(areaPage.getAreas());
              }
            })
        .autoStop()
        .setReplayFailedRequest(true)
        .setMaxReplays(5)
        .debug()
        .setThreads(1)
        .addSite(WebSite.of("www.stats.gov.cn").setRateLimiter(1, 1))
        .build()
        .start();

    for (Area area : areas) {
      areaMap.put(area.getCode(), area);
    }

    for (Area area : areas) {
      if (StrUtil.isNotBlank(area.getParentCode())) {
        area.setParent(areaMap.get(area.getParentCode()));
      } else {
        roots.add(area);
      }
    }

    for (Area area : areas) {
      area.setChildren(
          areas.stream()
              .filter(a -> area.getCode().equals(a.getParentCode()))
              .collect(Collectors.toList()));
    }

    List<List<String>> data = new ArrayList<>();
    data.add(ListUtil.of("统计用区划代码", "名称", "父级统计用区划代码", "父级名称"));
    addData(data, roots);
    ExcelWriter writer = ExcelUtil.getWriter("/Users/yann/Downloads/cities.xlsx");
    writer.write(data, true);
    writer.flush();
    writer.close();
  }

  private static void addData(List<List<String>> data, List<Area> areas) {
    if (areas != null) {
      for (Area area : areas) {
        data.add(
            ListUtil.toList(
                area.getCode(),
                area.getName(),
                area.getParentCode(),
                area.getParent() != null ? area.getParent().getName() : null));
        if (area.getChildren() != null) {
          addData(data, area.getChildren());
        }
      }
    }
  }
}
