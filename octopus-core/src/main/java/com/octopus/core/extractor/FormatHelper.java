package com.octopus.core.extractor;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.extractor.annotation.Format;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class FormatHelper {

  public static String format(String val, Format format) {
    if (StrUtil.isNotBlank(val) && StrUtil.isNotBlank(format.regex())) {
      String regex = format.regex();
      String formatter = format.format();
      if (StrUtil.isNotBlank(formatter)) {
        int[] groups =
            format.groups() == null || format.groups().length <= 0
                ? new int[] {0}
                : format.groups();
        List<String> args = new ArrayList<>();
        for (int group : groups) {
          args.add(ReUtil.get(regex, val, group));
        }
        return String.format(formatter, args.toArray());
      }
      return ReUtil.get(regex, val, 0);
    }
    return val;
  }
}
