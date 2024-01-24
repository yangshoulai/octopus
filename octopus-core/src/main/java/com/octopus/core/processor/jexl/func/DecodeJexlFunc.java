package com.octopus.core.processor.jexl.func;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.CharsetUtil;
import com.octopus.core.processor.jexl.JexlFunc;

import java.nio.charset.Charset;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/24
 */
public class DecodeJexlFunc implements JexlFunc {
    public String urlDecode(String content) {
        return URLDecoder.decode(content, CharsetUtil.CHARSET_UTF_8);
    }

    public String urlDecode(String content, String charset) {
        return URLDecoder.decode(content, Charset.forName(charset));
    }

    @Override
    public String getFuncName() {
        return "decoder";
    }
}
