package com.octopus.core.processor.configurable;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class FieldExtProperties implements Validator {

    private boolean ignoreError = true;

    private String[] booleanFalseValues;

    private String dateFormatPattern;

    private String dateFormatTimeZone;

    @Override
    public void validate() throws ValidateException {

    }
}
