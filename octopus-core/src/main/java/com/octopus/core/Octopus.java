package com.octopus.core;

import com.octopus.core.exception.OctopusException;

import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public interface Octopus {

    /**
     * 同步启动
     *
     * @throws OctopusException 启动异常
     */
    void start() throws OctopusException;

    /**
     * 异步启动
     *
     * @return future
     * @throws OctopusException 启动异常
     */
    Future<Void> startAsync() throws OctopusException;

    /**
     * 停止
     *
     * @throws OctopusException 启动异常
     */
    void stop() throws OctopusException;

    /**
     * 添加请求
     *
     * @param request 下载请求
     * @throws OctopusException 添加请求异常
     */
    void addRequest(Request request) throws OctopusException;

    /**
     * 新建构建器
     *
     * @return OctopusBuilder
     */
    static OctopusBuilder builder() {
        return new OctopusBuilder();
    }

    /**
     * 从yaml文件创建Octopus
     *
     * @param yaml yaml文件
     * @return Octopus
     */
    static Octopus fromYaml(InputStream yaml) {
        return OctopusBuilder.fromYaml(yaml).build();
    }

    /**
     * 从yaml文件创建Octopus
     *
     * @param yaml yaml文件
     * @return Octopus
     */
    static Octopus fromYaml(String yaml) {
        return OctopusBuilder.fromYaml(yaml).build();
    }
}
