package com.octopus.core.store;

import com.octopus.core.Request;
import com.octopus.core.replay.ReplayFilter;

import java.util.List;

/**
 * 请求存储器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public interface Store {

    /**
     * 获取一个请求
     *
     * @return 下载请求
     */
    Request get();

    /**
     * 存入一个请求
     *
     * @param request 下载请求
     * @return 操作是否成功
     */
    boolean put(Request request);

    /**
     * 判断任务是否已经存在
     *
     * @param request 任务
     * @return 是否存在
     */
    boolean exists(Request request);

    /**
     * 清空所有请求
     */
    void clear();

    /**
     * 标记已完成下载请求
     *
     * @param request 请求
     */
    void markAsCompleted(Request request);

    /**
     * 标记失败的下载请求
     *
     * @param request 请求
     * @param error   失败原因
     */
    void markAsFailed(Request request, String error);

    /**
     * 获取所有下载请求数量
     *
     * @return 下载请求数量
     */
    long getTotalSize();

    /**
     * 获取所有已完成的请求数量
     *
     * @return 已完成的请求数量
     */
    long getCompletedSize();

    /**
     * 获取所有待下载请求数量
     *
     * @return 待下载请求数量
     */
    long getWaitingSize();

    /**
     * 获取所有失败的请求数量
     *
     * @return 失败的请求数量
     */
    long getFailedSize();

    /**
     * 获取所有失败的下载请求
     *
     * @return 失败的下载请求
     */
    List<Request> getFailed();

    /**
     * 删除一个请求
     *
     * @param id 请求唯一编号
     */
    void delete(String id);

    /**
     * 重新将失败的请求放入待执行队列
     *
     * @param filter 请求过滤器
     * @return 重放的数量
     */
    int replayFailed(ReplayFilter filter);
}
