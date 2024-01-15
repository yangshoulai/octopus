package com.octopus.core.processor.extractor.convert;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.configurable.FieldExtProperties;

import java.nio.charset.Charset;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/15
 */
public class ByteArrayTypeConverter implements TypeConverter<byte[]> {
    @Override
    public byte[] convert(String source, FieldExtProperties ext) {
        return source.getBytes(StrUtil.isBlank(ext.getCharset()) ? CharsetUtil.CHARSET_UTF_8 : Charset.forName(ext.getCharset()));
    }
}
