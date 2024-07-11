package com.octopus.core;

import lombok.Getter;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
@Getter
public enum State {
    /**
     * 新建
     */
    NEW(1, "New"),
    /**
     * 启动中
     */
    STARTING(2, "Staring"),
    /**
     * 已启动
     */
    STARTED(3, "Started"),
    /**
     * 空闲
     */
    IDLE(4, "Idle"),
    /**
     * 停止中
     */
    STOPPING(5, "Stopping"),
    /**
     * 已停止
     */
    STOPPED(6, "Stopped");

    /**
     * 状态
     */
    private final int state;

    /**
     * 说明
     */
    private final String label;

    State(int state, String label) {
        this.state = state;
        this.label = label;
    }
}
