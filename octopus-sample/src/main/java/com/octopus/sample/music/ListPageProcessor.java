package com.octopus.sample.music;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlPath;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.AbstractProcessor;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.select.Elements;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
public class ListPageProcessor extends AbstractProcessor {

  public ListPageProcessor() {
    super(r -> r.getRequest().getUrl().contains("/discover/toplist"));
  }

  @Override
  public List<Request> process(Response response) {
    Elements elements = response.asDocument().select("ul.f-hide li a");
    return elements.stream()
        .map(
            el -> {
              String id = el.attr("href").replace("/song?id=", "");
              String name = el.text();
              Map<String, Object> params = new HashMap<>();
              params.put("ids", ListUtil.of(id));
              params.put("level", "standard");
              params.put("encodeType", "aac");
              params.put("csrf_token", "");
              Map<String, String> encryptParams = EncryptUtil.getEncryptParams(params);
              return Request.post(
                      UrlBuilder.create()
                          .setScheme("https")
                          .setHost(URLUtil.url(response.getRequest().getUrl()).getHost())
                          .setPath(UrlPath.of("/weapi/song/enhance/player/url/v1", null))
                          .build())
                  .setBody(HttpUtil.toParams(encryptParams).getBytes(StandardCharsets.UTF_8))
                  .addHeader(Header.CONTENT_TYPE.getValue(), ContentType.FORM_URLENCODED.getValue())
                  .putAttribute("name", name);
            })
        .collect(Collectors.toList());
  }
}
