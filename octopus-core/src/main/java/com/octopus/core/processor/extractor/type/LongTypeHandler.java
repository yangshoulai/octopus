package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.processor.extractor.FieldExt;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class LongTypeHandler implements TypeHandler<Long> {

    @Override
    public Long handle(String source, FieldExt ext) {
        try {
            if (StrUtil.isBlank(source)) {
                return null;
            }
            return NumberUtil.parseLong(source);
        } catch (Throwable e) {
            if (ext != null && !ext.ignoreError()) {
                throw new OctopusException("Can not parse [" + source + "] to long", e);
            }
            return null;
        }
    }

}
