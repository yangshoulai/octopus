package com.octopus.core.processor.matcher;

import cn.hutool.core.util.ReUtil;
import com.octopus.core.Response;
import lombok.NonNull;

import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * 基于响应头的正则匹配器
 * <p>
 * 检查Http响应头
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class AttrRegexMatcher implements Matcher {

    private final String attr;

    private final Pattern pattern;

    public AttrRegexMatcher(@NonNull String attr, @NonNull Pattern pattern) {
        this.attr = attr;
        this.pattern = pattern;
    }

    public AttrRegexMatcher(@NonNull String attr, @NonNull String regex) {
        this.attr = attr;
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean matches(Response response) {
        Object value = response.getRequest().getAttribute(attr);
        if (value != null) {
            return ReUtil.isMatch(this.pattern, value.toString());
        }
        return false;
    }
}
