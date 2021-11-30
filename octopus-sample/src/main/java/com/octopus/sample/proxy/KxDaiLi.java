package com.octopus.sample.proxy;

import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.LinkMethod;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import com.octopus.core.extractor.format.RegexFormat;
import com.octopus.core.extractor.format.SplitFormat;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/29
 */
@Slf4j
@Data
@Extractor
public class KxDaiLi {

  @Selector(expression = "table.active tbody tr", self = true)
  private List<KxDaiLiProxy> proxies;

  @LinkMethod
  public String nextPage(String url) {
    if (this.proxies != null && !this.proxies.isEmpty()) {
      int page = Integer.parseInt(ReUtil.get(".*/dailiip/1/(\\d+)\\.html$", url, 1));
      return "/dailiip/1/" + (++page) + ".html";
    }
    return null;
  }

  @Data
  @Extractor
  public static class KxDaiLiProxy {

    @Selector(type = Type.XPATH, expression = "//td[1]/text()")
    private String host;

    @Selector(type = Type.XPATH, expression = "//td[2]/text()")
    private int port;

    @Selector(type = Type.XPATH, expression = "//td[3]/text()")
    private String level;

    @Selector(type = Type.XPATH, expression = "//td[4]/text()")
    @SplitFormat
    private String[] types;

    @Selector(type = Type.XPATH, expression = "//td[5]/text()")
    @RegexFormat(regex = "^(.*) 秒$", groups = 1)
    private double responseSeconds;

    @Selector(type = Type.XPATH, expression = "//td[6]/text()")
    private String location;

    @Selector(type = Type.XPATH, expression = "//td[7]/text()")
    private String lastVerifyTime;
  }

  public static void main(String[] args) {
    Octopus.builder()
        .addProcessor(
            KxDaiLi.class,
            kxDaiLi -> {
              kxDaiLi
                  .getProxies()
                  .forEach(
                      p -> {
                        log.debug("{}", JSONUtil.toJsonStr(p));
                      });
            })
        .addSeeds("http://www.kxdaili.com/dailiip/1/1.html")
        .build()
        .start();
  }
}
