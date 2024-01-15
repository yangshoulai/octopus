package com.octopus.core.utils;

import com.octopus.core.exception.ValidateException;

/**
 * 可验证对象
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public interface Validatable {

    /**
     * 验证对象
     *
     * @throws ValidateException 验证异常
     */
    void validate() throws ValidateException;

}
