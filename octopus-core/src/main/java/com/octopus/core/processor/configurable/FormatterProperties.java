package com.octopus.core.processor.configurable;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class FormatterProperties implements Validator {

    private boolean trim = true;


    private boolean filter = true;


    private boolean split = false;


    private String separator = "；|;|，|,|#| |、|/|\\\\|\\|";


    private String regex = "";


    private String format = "%s";


    private int[] groups = {0};

    @Override
    public void validate() throws ValidateException {

    }
}
