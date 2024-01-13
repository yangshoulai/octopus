package com.octopus.core.exception;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public class OctopusException extends RuntimeException {

    public OctopusException(String message) {
        super(message);
    }

    public OctopusException(Throwable cause) {
        super(cause);
    }

    public OctopusException(String message, Throwable cause) {
        super(message, cause);
    }
}
