package com.octopus.core.processor.converter;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.configurable.FieldExtProperties;
import com.octopus.core.processor.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class BooleanConverter implements Converter<Boolean> {
    private static final List<String> FALSE_VALUES =
            ListUtil.toList("", "0", "非", "否", "off", "no", "f", "false");

    @Override
    public Boolean convert(String source, FieldExtProperties ext) {
        if (StrUtil.isBlank(source)) {
            return Boolean.FALSE;
        }
        if (ext.getBooleanFalseValues() != null && ext.getBooleanFalseValues().length > 0) {
            return !Arrays.stream(ext.getBooleanFalseValues()).collect(Collectors.toList()).contains(source.toLowerCase());
        }
        return !FALSE_VALUES.contains(source.toLowerCase());
    }
}
