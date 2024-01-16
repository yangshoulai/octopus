package com.octopus.core.processor.extractor;

import com.octopus.core.configurable.FieldExtProperties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public interface Converter<T> {

    T convert(String source, FieldExtProperties ext);

}
