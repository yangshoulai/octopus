package com.octopus.sample.kxdaili;

import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.LinkMethod;
import com.octopus.core.processor.extractor.selector.Css;
import com.octopus.core.processor.extractor.selector.Formatter;
import com.octopus.core.processor.extractor.selector.Xpath;
import com.octopus.core.processor.matcher.Matchers;
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

  @Css(expression = "table.active tbody tr", self = true)
  private List<KxDaiLiProxy> proxies;

  @LinkMethod
  public String nextPage(Response response) {
    if (this.proxies != null && !this.proxies.isEmpty()) {
      String url = response.getRequest().getUrl();
      int page = Integer.parseInt(ReUtil.get(".*/dailiip/1/(\\d+)\\.html$", url, 1));
      return "/dailiip/1/" + (++page) + ".html";
    }
    return null;
  }

  @Data
  @Extractor
  public static class KxDaiLiProxy {

    @Css("//td[1]/text()")
    private String host;

    @Css("//td[2]/text()")
    private int port;

    @Css("//td[3]/text()")
    private String level;

    @Css(expression = "//td[4]/text()", formatter = @Formatter(split = true))
    private String[] types;

    @Xpath(expression = "//td[5]/text()", formatter = @Formatter(regex = "^(.*) 秒$", groups = 1))
    private double responseSeconds;

    @Xpath(expression = "//td[6]/text()")
    private String location;

    @Xpath(expression = "//td[7]/text()")
    private String lastVerifyTime;
  }

  public static void main(String[] args) {
    Octopus.builder()
        .addProcessor(
            Matchers.ALL,
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
