package com.octopus.core.processor.configurable.convert;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.processor.configurable.FieldExtProperties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class CharacterTypeConverter implements TypeConverter<Character> {

    @Override
    public Character convert(String source, FieldExtProperties ext) {
        return StrUtil.isBlank(source) ? null : source.charAt(0);
    }
}
