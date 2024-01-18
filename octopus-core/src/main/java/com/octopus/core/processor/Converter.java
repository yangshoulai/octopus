package com.octopus.core.processor;

import com.octopus.core.properties.ConverterProperties;

/**
 * 转换器
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public interface Converter<T> {

    /**
     * 转换选择的内容为目标类型
     *
     * @param source 内容
     * @param ext    转换器配置
     * @return 目标类型
     */
    T convert(String source, ConverterProperties ext);

}
