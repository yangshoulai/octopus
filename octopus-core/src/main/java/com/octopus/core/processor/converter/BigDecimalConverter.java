package com.octopus.core.processor.converter;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.properties.FieldExtProperties;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.processor.Converter;

import java.math.BigDecimal;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/27
 */
public class BigDecimalConverter implements Converter<BigDecimal> {
    @Override
    public BigDecimal convert(String source, FieldExtProperties ext) {
        if (StrUtil.isNotEmpty(source)) {
            try {
                return new BigDecimal(source);
            } catch (Exception e) {
                if (ext != null && !ext.isIgnoreError()) {
                    throw new OctopusException("Can not parse [" + source + "] to big decimal", e);
                }
            }
        }
        return null;
    }
}
