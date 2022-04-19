package com.octopus.core.test;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.extractor.annotation.Body;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.extractor.selector.AttrSelector;
import com.octopus.core.extractor.selector.ParamSelector;
import com.octopus.core.extractor.selector.UrlSelector;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
@Data
@Extractor
public class OctopusTest {

  @UrlSelector private String url;

  @UrlSelector
  @RegexFormatter(regex = "^.*?\\?a=(\\d+)$", groups = 1)
  private String a;

  @UrlSelector
  @RegexFormatter(regex = "^.*?\\?a=(\\d+)$", groups = 1)
  private Integer a1;

  @ParamSelector(name = "a")
  private String a2;

  @AttrSelector(name = "b")
  private String b;

  @AttrSelector(name = "b")
  private Integer b1;

  @Body private byte[] bytes;

  public static void main(String[] args) {

    Octopus.builder()
        .addSeeds(Request.get("https://wwww.baidu.com?a=1").putAttribute("b", 2))
        .addProcessor(OctopusTest.class, System.out::println)
        .build()
        .start();
  }
}
