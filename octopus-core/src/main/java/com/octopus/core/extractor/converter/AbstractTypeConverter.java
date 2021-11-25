package com.octopus.core.extractor.converter;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.extractor.FormatHelper;
import com.octopus.core.extractor.annotation.Format;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public abstract class AbstractTypeConverter<T> implements TypeConverter<T> {

  @Override
  public T convert(String val, Annotation[] annotations) {
    if (StrUtil.isNotBlank(val)) {
      val = val.trim();
      List<Format> formats = this.findAnnotations(annotations, Format.class);
      if (!formats.isEmpty()) {
        for (Format format : formats) {
          val = FormatHelper.format(val, format);
        }
      }
      if (StrUtil.isNotBlank(val)) {
        return this.parse(val, annotations);
      }
    }
    return null;
  }

  protected abstract T parse(String val, Annotation[] annotations);

  @SuppressWarnings("unchecked")
  protected <F> List<F> findAnnotations(Annotation[] annotations, Class<F> type) {
    List<F> list = new ArrayList<>();
    if (annotations != null) {
      for (Annotation annotation : annotations) {
        if (type.isAssignableFrom(annotation.getClass())) {
          list.add((F) annotation);
        }
      }
    }
    return list;
  }
}
