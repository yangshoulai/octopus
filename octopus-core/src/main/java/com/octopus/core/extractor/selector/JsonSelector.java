package com.octopus.core.extractor.selector;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.octopus.core.extractor.annotation.Selector;
import java.util.ArrayList;
import java.util.List;
import net.minidev.json.JSONArray;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/27
 */
public class JsonSelector extends CacheableSelector<String> {

  @Override
  public List<String> selectWithType(String json, Selector selector) {
    List<String> list = new ArrayList<>();
    Configuration configuration =
        Configuration.builder()
            .options(Option.ALWAYS_RETURN_LIST)
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();
    Object obj = JsonPath.using(configuration).parse(json).read(selector.expression());
    if (obj instanceof JSONArray) {
      JSONArray array = (JSONArray) obj;
      for (Object o : array) {
        list.add(o.toString());
      }
    }
    return list;
  }

  @Override
  protected String parse(String content) {
    return content;
  }
}
