package com.octopus.sample.music;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.impl.MatchableProcessor;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
public class PlayerUrlProcessor extends MatchableProcessor {

  public PlayerUrlProcessor() {
    super(r -> r.getRequest().getUrl().contains("/enhance/player"));
  }

  @Override
  public void process(Response response, Octopus octopus) {
    if (StrUtil.isNotBlank(response.asText())) {
      JSONObject json = (JSONObject) response.asJson();
      JSONArray data = json.getJSONArray("data");
      if (data != null && data.size() > 0) {
        String url = data.getJSONObject(0).getStr("url");
        if (StrUtil.isNotBlank(url)) {
          octopus.addRequest(Request.get(url).setPriority(1));
        }
      }
    }
  }
}
