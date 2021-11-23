package com.octopus.sample.wallhaven;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.AbstractProcessor;
import com.octopus.core.processor.matcher.Matchers;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/1/4
 */
@Slf4j
public class ListPageProcessor extends AbstractProcessor {

  private static final Pattern THUMBNAIL_PATTERN =
      Pattern.compile(".*th\\.wallhaven\\.cc/small/(\\w+)/((\\w|\\.)+)");

  public ListPageProcessor() {
    super(Matchers.HTML);
  }

  @Override
  public List<Request> process(Response response) {
    Document document = response.asDocument();
    Elements imgs = document.select("#thumbs ul li img");
    List<Request> requests =
        imgs.stream()
            .map(
                e -> {
                  Element p = e.parent();
                  String src = e.attr("data-src");
                  Matcher matcher = THUMBNAIL_PATTERN.matcher(src);
                  if (matcher.matches()) {
                    boolean isPng = p.select("> .thumb-info > span.png").size() > 0;
                    String name = matcher.group(2);
                    if (isPng) {
                      name = name.substring(0, name.indexOf(".")) + ".png";
                    }
                    return String.format(
                        "https://w.wallhaven.cc/full/%s/wallhaven-%s", matcher.group(1), name);
                  } else {
                    log.debug("Url [{}] does not match thumbnail pattern", src);
                  }
                  return null;
                })
            .filter(StrUtil::isNotBlank)
            .map(url -> Request.get(url).setRepeatable(false))
            .collect(Collectors.toList());
    Element next = document.select("ul.pagination li a.next").first();
    if (next != null) {
      requests.add(Request.get(next.attr("href")).setPriority(1));
    }
    return requests;
  }
}
