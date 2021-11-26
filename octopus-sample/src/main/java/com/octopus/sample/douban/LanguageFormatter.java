package com.octopus.sample.douban;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.extractor.format.Formatter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public class LanguageFormatter implements Formatter<LanguageFormat> {

  @Override
  public String format(String val, LanguageFormat format) {
    return Arrays.stream(val.split("/"))
        .map(StrUtil::trim)
        .sorted()
        .collect(Collectors.joining(","));
  }
}
