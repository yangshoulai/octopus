package com.octopus.sample.bing;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.AbstractProcessor;
import com.octopus.core.processor.matcher.Matchers;
import java.util.List;
import org.jsoup.nodes.Document;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/1/15
 */
public class DetailPageProcessor extends AbstractProcessor {

  public DetailPageProcessor() {
    super(
        Matchers.and(
            Matchers.HTML,
            response ->
                response.getRequest().getUrl().contains("/photo/")
                    && response.getRequest().getUrl().contains("force=")));
  }

  @Override
  public List<Request> process(Response response) {
    Document document = response.asDocument();
    String href = document.select("a.ctrl.download").first().attr("href");
    if (StrUtil.isNotBlank(href)) {
      String url = "https://bing.ioliu.cn/" + href;
      return ListUtil.of(Request.get(url).setPriority(1));
    }
    return null;
  }
}
