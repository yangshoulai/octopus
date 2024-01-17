package com.octopus.core.processor.converter;

import com.octopus.core.properties.FieldExtProperties;
import com.octopus.core.processor.Converter;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class CharSequenceConverter implements Converter<CharSequence> {
    @Override
    public CharSequence convert(String source, FieldExtProperties ext) {
        return source;
    }
}
