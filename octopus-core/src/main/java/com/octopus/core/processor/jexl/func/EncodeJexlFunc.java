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
public class EncodeJexlFunc implements JexlFunc {
    public String urlEncode(String content, String charset) {
        return URLEncodeUtil.encode(content, Charset.forName(charset));
    }

    public String urlEncode(String content) {
        return URLEncodeUtil.encode(content);
    }

    @Override
    public String getFuncName() {
        return "encoder";
    }
}
