package com.octopus.core.processor.extractor.converter;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.configurable.FieldExtProperties;
import com.octopus.core.processor.extractor.Converter;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class CharacterConverter implements Converter<Character> {

    @Override
    public Character convert(String source, FieldExtProperties ext) {
        return StrUtil.isBlank(source) ? null : source.charAt(0);
    }
}
