package com.octopus.core.processor.extractor.convert;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.configurable.FieldExtProperties;
import com.octopus.core.exception.OctopusException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class LongTypeConverter implements TypeConverter<Long> {
    @Override
    public Long convert(String source, FieldExtProperties ext) {
        try {
            if (StrUtil.isBlank(source)) {
                return null;
            }
            return NumberUtil.parseLong(source);
        } catch (Throwable e) {
            if (!ext.isIgnoreError()) {
                throw new OctopusException("Can not parse [" + source + "] to long", e);
            }
            return null;
        }
    }
}
