package com.octopus.core.replay;

import com.octopus.core.Request;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/1/11
 */
public interface ReplayFilter {

    /**
     * 过滤请求是否能够重放
     *
     * @param request 请求
     * @return true 允许重放，false 不允许重放
     */
    boolean filter(Request request);

}
