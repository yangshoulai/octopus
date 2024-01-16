package com.octopus.core.processor.matcher;

import com.octopus.core.Response;

import java.util.List;

import lombok.NonNull;

/**
 * 逻辑或匹配器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class OrMatcher implements Matcher {
    private final List<Matcher> matchers;

    public OrMatcher(@NonNull List<Matcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Response response) {
        return this.matchers.stream().anyMatch(matcher -> matcher.matches(response));
    }
}
