package com.octopus.core.processor.configurable.convert;

import com.octopus.core.processor.configurable.FieldExtProperties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public interface TypeConverter<T> {

    T convert(String source, FieldExtProperties ext);

}
