package com.octopus.core.processor.extractor.type;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class DateTypeHandler implements TypeHandler<Date> {

  @Override
  public Date handle(String source, Annotation annotation) {
    DateType dateType = (DateType) annotation;
    String format =
        dateType == null || StrUtil.isBlank(dateType.pattern())
            ? DatePattern.NORM_DATETIME_PATTERN
            : dateType.pattern();
    try {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
      if(dateType != null && StrUtil.isNotEmpty(dateType.timeZone())){
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(dateType.timeZone()));
      }
      return simpleDateFormat.parse(source);
    } catch (Throwable e) {
      if (dateType != null && !dateType.ignorable()) {
        throw new OctopusException(
            "Can not parse [" + source + "] to date with pattern [" + format + "]", e);
      }
      return null;
    }
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return DateType.class;
  }
}
