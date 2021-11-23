package com.octopus.sample.bing;

import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.AbstractProcessor;
import com.octopus.core.processor.matcher.Matchers;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/1/15
 */
public class ListPageProcessor extends AbstractProcessor {

  public ListPageProcessor() {
    super(
        Matchers.and(
            Matchers.HTML, response -> !response.getRequest().getUrl().contains("/photo/")));
  }

  @Override
  public List<Request> process(Response response) {
    boolean firstPage = response.getRequest().getAttribute("firstPage", false);
    Document document = response.asDocument();
    List<Request> newRequests = new ArrayList<>();
    document.select("div.container > div.item > div.card > a.mark").eachAttr("href").stream()
        .map(href -> "https://bing.ioliu.cn" + href)
        .map(url -> Request.get(url).setPriority(5))
        .forEach(newRequests::add);

    if (firstPage) {
      int pages =
          Integer.parseInt(
              document.select("div.page > span").first().text().replace("1 / ", "").trim());
      for (int i = 2; i <= pages; i++) {
        newRequests.add(Request.get("https://bing.ioliu.cn/?p=" + i));
      }
    }
    return newRequests;
  }
}
