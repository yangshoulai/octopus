package com.octopus.core.extractor.format;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public class RegexFormatterHandler implements FormatterHandler<RegexFormatter> {
  @Override
  public String format(String val, RegexFormatter format, Response response) {
    if (StrUtil.isBlank(val)) {
      return format == null ? val : format.def();
    }
    if (format == null) {
      return val;
    }
    int[] groups =
        format.groups() == null || format.groups().length <= 0 ? new int[] {0} : format.groups();
    List<String> args = new ArrayList<>();
    for (int group : groups) {
      String groupVal = ReUtil.get(format.regex(), val, group);
      args.add(groupVal == null ? "" : groupVal);
    }
    return String.format(format.format(), args.toArray());
  }
}
