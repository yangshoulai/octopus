package com.octopus.sample.area;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.extractor.selector.AttrSelector;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.LinkMethod;
import com.octopus.core.extractor.selector.XpathSelector;
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

  @XpathSelector(expression = "//td[1]//text()")
  private String code;

  @XpathSelector(expression = "//td[last()]//text()")
  private String name;

  @XpathSelector(expression = "//td[1]//a/@href")
  private String nextUrl;

  @AttrSelector(name = "parent")
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
