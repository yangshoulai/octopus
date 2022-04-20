package com.octopus.core.processor.extractor.convertor;

import cn.hutool.core.util.ClassUtil;
import com.octopus.core.Response;
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
   * @param response 原始响应
   * @return 转换后的数据
   */
  T convert(String val, A format, Response response);

  /**
   * 转换器支持的数据类型
   *
   * @return 支持的数据类型
   */
  Class<?>[] getSupportClasses();

  /**
   * 获取支持的转换类型
   *
   * @return 支持的转换类型
   */
  default Class<?> getSupportedType() {
    return ClassUtil.getTypeArgument(this.getClass(), 0);
  }

  /**
   * 获取支持的转化注解类型
   *
   * @return 支持的转化注解类型
   */
  @SuppressWarnings("unchecked")
  default Class<? extends Annotation> getSupportedAnnotationType() {
    return (Class<? extends Annotation>) ClassUtil.getTypeArgument(this.getClass(), 1);
  }
}
