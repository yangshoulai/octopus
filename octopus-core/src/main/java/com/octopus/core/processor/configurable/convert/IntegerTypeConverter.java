package com.octopus.core.processor.configurable.convert;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.processor.configurable.FieldExtProperties;
import com.octopus.core.exception.OctopusException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class IntegerTypeConverter implements TypeConverter<Integer> {

    @Override
    public Integer convert(String source, FieldExtProperties ext) {
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
