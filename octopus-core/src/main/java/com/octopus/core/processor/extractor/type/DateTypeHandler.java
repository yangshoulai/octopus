package com.octopus.core.processor.extractor.type;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.processor.extractor.FieldExt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class DateTypeHandler implements TypeHandler<Date> {

    @Override
    public Date handle(String source, FieldExt ext) {
        String format =
                ext == null || StrUtil.isBlank(ext.dateFormatPattern())
                        ? DatePattern.NORM_DATETIME_PATTERN
                        : ext.dateFormatPattern();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            if (ext != null && StrUtil.isNotBlank(ext.dateFormatTimeZone())) {
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ext.dateFormatTimeZone()));
            }
            return simpleDateFormat.parse(source);
        } catch (Throwable e) {
            if (ext != null && !ext.ignoreError()) {
                throw new OctopusException(
                        "Can not parse [" + source + "] to date with pattern [" + format + "]", e);
            }
            return null;
        }
    }

}
