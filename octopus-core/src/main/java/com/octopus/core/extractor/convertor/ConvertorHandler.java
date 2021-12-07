package com.octopus.core.extractor.convertor;

import java.lang.annotation.Annotation;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public interface ConvertorHandler<T, A extends Annotation> {

  /**
   * 将提取出来的字符串转换成特定的数据类型
   *
   * @param val 字符串
   * @param format 注解
   * @return 转换后的数据
   */
  T convert(String val, A format);

  /**
   * 转换器支持的数据类型
   *
   * @return 支持的数据类型
   */
  Class<?>[] getSupportClasses();
}
