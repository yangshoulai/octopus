package com.octopus.core.processor;

import com.octopus.core.Response;

/**
 * 收集器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/12/27
 */
public interface Collector<R> {

    /**
     * 提取结果收集
     *
     * @param result   结果
     * @param response 响应
     */
    void collect(R result, Response response);
}
