package com.octopus.core.processor.jexl.func;

import cn.hutool.core.util.ReUtil;
import com.octopus.core.processor.jexl.JexlFunc;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/24
 */
public class RegexJexlFunc implements JexlFunc {

    public boolean isMatch(String regex, String content) {
        return ReUtil.isMatch(regex, content);
    }

    public String get(String regex, String content, int group) {
        return ReUtil.get(regex, content, group);
    }

    @Override
    public String getFuncName() {
        return "regex";
    }
}
