package com.octopus.core.processor.extractor.type;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import java.lang.annotation.Annotation;
import java.util.Date;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class DateTypeHandler implements TypeHandler<Date> {

  @Override
  public Date handle(String source, Annotation annotation) {
    DateType dateType = (DateType) annotation;
    String format = dateType == null ? DatePattern.NORM_DATETIME_PATTERN : dateType.pattern();
    return DateUtil.parse(source, format);
  }

  @Override
  public Class<? extends Annotation> getSupportAnnotationType() {
    return DateType.class;
  }
}
