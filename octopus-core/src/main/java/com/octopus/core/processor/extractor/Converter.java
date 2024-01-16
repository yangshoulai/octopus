package com.octopus.core.processor.extractor;

import com.octopus.core.configurable.FieldExtProperties;

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
    T convert(String source, FieldExtProperties ext);

}
