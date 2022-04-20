package com.octopus.core.processor.extractor;

import com.octopus.core.exception.OctopusException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/28
 */
public class InvalidExtractorException extends OctopusException {
    public InvalidExtractorException(String message) {
        super(message);
    }

    public InvalidExtractorException(Throwable cause) {
        super(cause);
    }

    public InvalidExtractorException(String message, Throwable cause) {
        super(message, cause);
    }
}
