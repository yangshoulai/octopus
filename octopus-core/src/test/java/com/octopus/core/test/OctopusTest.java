package com.octopus.core.test;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.extractor.annotation.*;
import com.octopus.core.extractor.format.RegexFormatter;
import lombok.Data;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
@Data
@Extractor
public class OctopusTest {

  @Url private String url;

  @Url
  @RegexFormatter(regex = "^.*?\\?a=(\\d+)$", groups = 1)
  private String a;

  @Url
  @RegexFormatter(regex = "^.*?\\?a=(\\d+)$", groups = 1)
  private Integer a1;

  @Param(name = "a")
  private String a2;

  @Attr(name = "b")
  private String b;

  @Attr(name = "b")
  private Integer b1;

  @Body private byte[] bytes;

  public static void main(String[] args) {

    Octopus.builder()
        .addSeeds(Request.get("https://wwww.baidu.com?a=1").putAttribute("b", 2))
        .addProcessor(
            OctopusTest.class,
            octopusTest -> {
              System.out.println(octopusTest);
            })
        .build()
        .start();
  }
}
