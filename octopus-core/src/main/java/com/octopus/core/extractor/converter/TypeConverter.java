package com.octopus.core.extractor.converter;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public interface TypeConverter<T> {

  /**
   * 将提取出来的字符串转换成特定的数据类型
   *
   * @param val 字符串
   * @param annotations 注解
   * @return 转换后的数据
   */
  T convert(String val, Annotation[] annotations);

  /**
   * 转换器支持的数据类型
   *
   * @return 支持的数据类型
   */
  Class<?>[] supportClasses();
}
