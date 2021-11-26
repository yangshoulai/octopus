package com.octopus.sample.gitee;

import cn.hutool.core.util.NumberUtil;
import com.octopus.core.extractor.format.Formatter;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public class StarFormatter implements Formatter<StarFormat> {

  @Override
  public String format(String val, StarFormat format) {
    if (val.contains("k") || val.contains("K")) {
      val = val.replaceAll("k|K", "");
      val = NumberUtil.toStr(NumberUtil.parseNumber(val).floatValue() * 1000);
    }
    return val;
  }
}
