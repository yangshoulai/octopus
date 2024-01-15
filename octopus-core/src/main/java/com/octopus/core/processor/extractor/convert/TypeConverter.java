package com.octopus.core.processor.extractor.convert;

import com.octopus.core.configurable.FieldExtProperties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public interface TypeConverter<T> {

    T convert(String source, FieldExtProperties ext);

}
