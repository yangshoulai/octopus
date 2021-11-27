package com.octopus.core.extractor.convertor;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
@Slf4j
public class DateConvertor implements Convertor<Date, DateVal> {

  @Override
  public Date convert(String val, DateVal format) {
    if (StrUtil.isBlank(val)) {
      return null;
    }
    String pattern = null;
    if (format != null) {
      pattern = format.pattern();
    }
    try {
      if (NumberUtil.isLong(val)) {
        return new Date(Long.parseLong(val));
      }
      return DateUtil.parse(
          val, StrUtil.isNotBlank(pattern) ? pattern : DatePattern.NORM_DATETIME_PATTERN);
    } catch (Exception e) {
      if (format != null && !format.ignorable()) {
        throw new OctopusException(e);
      } else {
        log.debug("", e);
      }
    }
    return null;
  }

  @Override
  public Class<?>[] getSupportClasses() {
    return new Class[] {Date.class};
  }
}
