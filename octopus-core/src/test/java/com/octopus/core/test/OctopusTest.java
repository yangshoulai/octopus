package com.octopus.core.test;

import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.selector.Formatter;
import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.extractor.selector.Selector.Type;
import com.octopus.core.processor.matcher.Matchers;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
@Data
@Extractor
public class OctopusTest {

  @Selector(type = Type.Url)
  private String url;

  @Selector(type = Type.Url, formatters = @Formatter(regex = "^.*?\\?a=(\\d+)$", groups = 1))
  private String a;

  @Selector(type = Type.Url, formatters = @Formatter(regex = "^.*?\\?a=(\\d+)$", groups = 1))
  private Integer a1;

  @Selector(type = Type.Param, value = "a")
  private String a2;

  @Selector(type = Type.Attr, value = "b")
  private String b;

  @Selector(type = Type.Attr, value = "b")
  private Integer b1;

  public static void main(String[] args) {

    Octopus.builder()
        .addSeeds(Request.get("https://wwww.baidu.com?a=1").putAttribute("b", 2))
        .addProcessor(Matchers.ALL, OctopusTest.class, System.out::println)
        .build()
        .start();
  }
}
