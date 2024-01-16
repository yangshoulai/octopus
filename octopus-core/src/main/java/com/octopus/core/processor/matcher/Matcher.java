package com.octopus.core.processor.matcher;

import com.octopus.core.Response;

/**
 * 响应匹配器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public interface Matcher {

    /**
     * 是否匹配
     *
     * @param response 响应
     * @return 是否匹配
     */
    boolean matches(Response response);
}
