package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.processor.extractor.FieldExt;

import java.math.BigDecimal;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/27
 */
public class BigDecimalTypeHandler implements TypeHandler<BigDecimal> {

    @Override
    public BigDecimal handle(String source, FieldExt ext) {
        if (StrUtil.isNotEmpty(source)) {
            try {
                return new BigDecimal(source);
            } catch (Exception e) {
                if (ext != null && !ext.ignoreError()) {
                    throw new OctopusException("Can not parse [" + source + "] to big decimal", e);
                }
            }
        }
        return null;
    }

}
