package com.octopus.core.processor.converter;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.properties.selector.ConverterProperties;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.processor.Converter;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class IntegerConverter implements Converter<Integer> {
    @Override
    public Integer convert(String source, ConverterProperties ext) {
        try {
            if (StrUtil.isBlank(source)) {
                return null;
            }
            return NumberUtil.parseInt(source);
        } catch (Throwable e) {
            if (!ext.isIgnoreError()) {
                throw new OctopusException("Can not parse [" + source + "] to integer", e);
            }
            return null;
        }
    }
}
