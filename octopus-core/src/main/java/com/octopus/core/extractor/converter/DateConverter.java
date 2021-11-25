package com.octopus.core.extractor.converter;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.extractor.annotation.DateType;
import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Optional;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class DateConverter extends AbstractTypeConverter<Date> {

  @Override
  protected Date parse(String val, Annotation[] annotations) {
    String pattern = null;

    if (annotations != null) {
      Optional<String> optional =
          this.findAnnotations(annotations, DateType.class).stream()
              .map(DateType::pattern)
              .findFirst();
      if (optional.isPresent()) {
        pattern = optional.get();
      }
    }
    return DateUtil.parse(
        val, StrUtil.isNotBlank(pattern) ? pattern : DatePattern.NORM_DATETIME_PATTERN);
  }

  @Override
  public Class<?>[] supportClasses() {
    return new Class[] {Date.class};
  }
}
