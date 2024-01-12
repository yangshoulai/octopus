package com.octopus.core.utils;

import com.octopus.core.exception.ValidateException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public interface Validator {

    void validate() throws ValidateException;

}
