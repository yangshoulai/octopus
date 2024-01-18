package com.octopus.core.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;

import java.util.Collection;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/17
 */
public class Validator {

    public static void gt(double target, double value, String error) throws ValidateException {
        if (target <= value) {
            throw new ValidateException(error);
        }
    }
    public static void gt(int target, int value, String error) throws ValidateException {
        if (target <= value) {
            throw new ValidateException(error);
        }
    }

    public static void eq(int target, int value, String error) throws ValidateException {
        if (target != value) {
            throw new ValidateException(error);
        }
    }

    public static void notBlank(String target, String error) throws ValidateException {
        if (StrUtil.isBlank(target)) {
            throw new ValidateException(error);
        }
    }

    public static void notEmpty(Object target, String error) throws ValidateException {
        if (ObjectUtil.isEmpty(target)) {
            throw new ValidateException(error);
        }
        if (target instanceof Validatable) {
            ((Validatable) target).validate();
        }
    }

    public static void validateWhenNotNull(Validatable validatable) throws ValidateException {
        if (validatable != null) {
            validatable.validate();
        }
    }

    public static <V extends Validatable> void validateWhenNotNull(V[] validatables) throws ValidateException {
        if (validatables != null) {
            for (Validatable validatable : validatables) {
                validateWhenNotNull(validatable);
            }
        }
    }

    public static <V extends Validatable> void validateWhenNotNull(Collection<V> validatables) throws ValidateException {
        if (validatables != null) {
            for (Validatable validatable : validatables) {
                validateWhenNotNull(validatable);
            }
        }
    }
}
