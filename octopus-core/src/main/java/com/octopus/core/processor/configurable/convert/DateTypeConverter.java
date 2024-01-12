package com.octopus.core.processor.configurable.convert;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.processor.configurable.FieldExtProperties;
import com.octopus.core.exception.OctopusException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class DateTypeConverter implements TypeConverter<Date> {


    @Override
    public Date convert(String source, FieldExtProperties ext) {
        String pattern = ext.getDateFormatPattern();
        String timeZone = ext.getDateFormatTimeZone();
        String format = StrUtil.isBlank(pattern) ? DatePattern.NORM_DATETIME_PATTERN : pattern;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            if (StrUtil.isNotBlank(timeZone)) {
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            }
            return simpleDateFormat.parse(source);
        } catch (Throwable e) {
            if (!ext.isIgnoreError()) {
                throw new OctopusException(
                        "Can not parse [" + source + "] to date with pattern [" + format + "]", e);
            }
            return null;
        }
    }
}
