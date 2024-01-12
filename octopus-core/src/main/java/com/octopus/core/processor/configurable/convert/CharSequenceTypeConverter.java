package com.octopus.core.processor.configurable.convert;

import com.octopus.core.processor.configurable.FieldExtProperties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class CharSequenceTypeConverter implements TypeConverter<CharSequence> {

    @Override
    public CharSequence convert(String source, FieldExtProperties ext) {
        return source;
    }
}
