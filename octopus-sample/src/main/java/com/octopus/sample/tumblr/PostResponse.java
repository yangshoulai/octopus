package com.octopus.sample.tumblr;

import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.LinkMethod;
import com.octopus.core.extractor.annotation.Selector;
import com.octopus.core.extractor.annotation.Selector.Type;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
@Data
@Extractor
@Link(
    selector = @Selector(type = Type.JSON, expression = "$.response.posts[*].content[*].media.url"))
@Link(
    selector =
        @Selector(type = Type.JSON, expression = "$.response.posts[*].content[*].media[0].url"))
public class PostResponse {

  @Selector(type = Type.JSON, expression = "$.meta.status")
  private int status;

  @Selector(type = Type.JSON, expression = "$.response.total_posts")
  private int totalPosts;

  @Selector(type = Type.JSON, expression = "$.response.posts[*].content[*].media.url")
  private String[] videos;

  @Selector(type = Type.JSON, expression = "$.response.posts[*].content[*].media[*].url")
  private String[] photos;

  @LinkMethod
  public Request getNextPagePosts(Response response) {
    int pageNumber = Integer.parseInt(response.getRequest().getParams().get("page_number"));
    int offset = Integer.parseInt(response.getRequest().getParams().get("offset"));
    if (offset < this.totalPosts) {
      Map<String, String> params = new HashMap<>(response.getRequest().getParams());
      params.put("offset", String.valueOf(offset += 20));
      params.put("page_number", String.valueOf(++pageNumber));
      return Request.get(response.getRequest().getUrl())
          .setParams(params)
          .addHeaders(response.getRequest().getHeaders());
    }
    return null;
  }
}
