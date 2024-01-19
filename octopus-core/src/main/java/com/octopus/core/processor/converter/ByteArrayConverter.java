package com.octopus.core.processor.converter;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.properties.selector.ConverterProperties;
import com.octopus.core.processor.Converter;

import java.nio.charset.Charset;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/15
 */
public class ByteArrayConverter implements Converter<byte[]> {
    @Override
    public byte[] convert(String source, ConverterProperties ext) {
        return source.getBytes(StrUtil.isBlank(ext.getCharset()) ? CharsetUtil.CHARSET_UTF_8 : Charset.forName(ext.getCharset()));
    }
}
