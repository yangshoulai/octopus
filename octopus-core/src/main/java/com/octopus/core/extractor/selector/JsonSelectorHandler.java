package com.octopus.core.extractor.selector;

import cn.hutool.json.JSONUtil;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minidev.json.JSONArray;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
public class JsonSelectorHandler extends CacheableSelector<String, JsonSelector> {

  private static final Configuration CONFIGURATION =
      Configuration.builder()
          .options(Option.ALWAYS_RETURN_LIST)
          .options(Option.SUPPRESS_EXCEPTIONS)
          .build();

  @Override
  public List<String> selectWithType(String json, JsonSelector selector) {
    List<String> list = new ArrayList<>();
    Object obj = JsonPath.using(CONFIGURATION).parse(json).read(selector.expression());
    if (obj instanceof JSONArray) {
      JSONArray array = (JSONArray) obj;
      for (Object o : array) {
        if (o instanceof Map || o instanceof List) {
          list.add(JSONUtil.toJsonStr(o));
        } else {
          list.add(o.toString());
        }
      }
    }
    return filterResults(list, selector.filter(), selector.trim(), selector.multi());
  }

  @Override
  protected String parse(String content) {
    return content;
  }
}
