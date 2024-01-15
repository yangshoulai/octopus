package com.octopus.core.utils;

/**
 * 转换当前对象为其他对象
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/15
 */
public interface Transformable<T> {

    /**
     * 转变为目标对象
     *
     * @return 目标对象
     */
    T transform();
}
