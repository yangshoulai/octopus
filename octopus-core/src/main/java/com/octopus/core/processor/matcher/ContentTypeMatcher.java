package com.octopus.core.processor.matcher;

import java.util.regex.Pattern;

import lombok.NonNull;

/**
 * 基于响应格式的正则匹配器
 * <p>
 * 检查Http响应头Content-Type
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class ContentTypeMatcher extends HeaderRegexMatcher {

    public ContentTypeMatcher(@NonNull Pattern pattern) {
        super("Content-Type", pattern);
    }

    public ContentTypeMatcher(@NonNull String regex) {
        super("Content-Type", regex);
    }
}
