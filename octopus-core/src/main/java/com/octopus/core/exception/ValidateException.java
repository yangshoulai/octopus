package com.octopus.core.exception;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public class ValidateException extends OctopusException {
    public ValidateException(String message) {
        super(message);
    }

    public ValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidateException(Throwable cause) {
        super(cause);
    }
}
