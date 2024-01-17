package com.octopus.core.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/17
 */
public class Validator {

    public static void notBlank(String target, String error) throws ValidateException {
        if (StrUtil.isBlank(target)) {
            throw new ValidateException(error);
        }
    }

    public static void notEmpty(Object target, String error) throws ValidateException {
        if (ObjectUtil.isEmpty(target)) {
            throw new ValidateException(error);
        }
    }

    public static void validateWhenNotNull(Validatable validatable) throws ValidateException {
        if (validatable != null) {
            validatable.validate();
        }
    }
}
