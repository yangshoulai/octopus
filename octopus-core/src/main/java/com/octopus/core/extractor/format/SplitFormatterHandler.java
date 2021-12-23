package com.octopus.core.extractor.format;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
public class SplitFormatterHandler implements MultiLineFormatterHandler<SplitFormatter> {

  @Override
  public List<String> format(String val, SplitFormatter format, Response response) {
    if (StrUtil.isBlank(val)) {
      return null;
    }
    Stream<String> stream = Arrays.stream(val.split(format.regex(), format.limit()));
    if (format.filter()) {
      stream = stream.filter(StrUtil::isNotBlank);
    }
    if (format.trim()) {
      stream = stream.map(StrUtil::trim);
    }
    return stream.collect(Collectors.toList());
  }
}
