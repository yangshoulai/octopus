package com.octopus.sample.music;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.AbstractProcessor;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
public class PlayerUrlProcessor extends AbstractProcessor {

  public PlayerUrlProcessor() {
    super(r -> r.getRequest().getUrl().contains("/enhance/player"));
  }

  @Override
  public List<Request> process(Response response) {
    if (StrUtil.isNotBlank(response.asText())) {
      JSONObject json = (JSONObject) response.asJson();
      JSONArray data = json.getJSONArray("data");
      if (data != null && data.size() > 0) {
        String url = data.getJSONObject(0).getStr("url");
        return StrUtil.isBlank(url) ? null : ListUtil.of(Request.get(url).setPriority(1));
      }
    }
    return null;
  }
}
