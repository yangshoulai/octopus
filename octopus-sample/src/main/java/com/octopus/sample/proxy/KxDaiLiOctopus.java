package com.octopus.sample.proxy;

import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Response;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.LinkMethod;
import com.octopus.core.extractor.format.RegexFormatter;
import com.octopus.core.extractor.format.SplitFormatter;
import com.octopus.core.extractor.selector.CssSelector;
import com.octopus.core.extractor.selector.XpathSelector;
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
public class KxDaiLiOctopus {

  @CssSelector(expression = "table.active tbody tr", self = true)
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

    @XpathSelector(expression = "//td[1]/text()")
    private String host;

    @XpathSelector(expression = "//td[2]/text()")
    private int port;

    @XpathSelector(expression = "//td[3]/text()")
    private String level;

    @XpathSelector(expression = "//td[4]/text()")
    @SplitFormatter
    private String[] types;

    @XpathSelector(expression = "//td[5]/text()")
    @RegexFormatter(regex = "^(.*) 秒$", groups = 1)
    private double responseSeconds;

    @XpathSelector(expression = "//td[6]/text()")
    private String location;

    @XpathSelector(expression = "//td[7]/text()")
    private String lastVerifyTime;
  }

  public static void main(String[] args) {
    Octopus.builder()
        .addProcessor(
            KxDaiLiOctopus.class,
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
