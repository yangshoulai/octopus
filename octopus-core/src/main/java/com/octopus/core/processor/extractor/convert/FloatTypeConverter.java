package com.octopus.core.processor.extractor.convert;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.processor.extractor.configurable.FieldExtProperties;
import com.octopus.core.exception.OctopusException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class FloatTypeConverter implements TypeConverter<Float> {

    @Override
    public Float convert(String source, FieldExtProperties ext) {
        try {
            if (StrUtil.isBlank(source)) {
                return null;
            }
            return NumberUtil.parseFloat(source);
        } catch (Throwable e) {
            if (!ext.isIgnoreError()) {
                throw new OctopusException("Can not parse [" + source + "] to float", e);
            }
            return null;
        }
    }
}
