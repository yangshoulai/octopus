package com.octopus.sample.area;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.LinkMethod;
import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.extractor.selector.Selector.Type;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/3/30
 */
@Extractor
@Data
public class Area {

  @Selector(type = Type.Xpath, value = "//td[1]//text()")
  private String code;

  @Selector(type = Type.Xpath, value = "//td[last()]//text()")
  private String name;

  @Selector(type = Type.Xpath, value = "//td[1]//a/@href")
  private String nextUrl;

  @Selector(type = Type.Attr, value = "parent")
  private String parentCode;

  private Area parent;

  private List<Area> children = new ArrayList<>();

  @LinkMethod
  public Request getNextRequest(Response response) {
    if (StrUtil.isNotBlank(this.nextUrl)) {
      String preUrl = response.getRequest().getUrl();

      return Request.get(preUrl.substring(0, preUrl.lastIndexOf("/")) + "/" + this.nextUrl)
          .setRepeatable(false)
          .putAttribute("parent", this.code);
    }
    return null;
  }
}
