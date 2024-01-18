package com.octopus.core.properties.selector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validatable;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/17
 */
@Data
public class DenoiserProperties implements Validatable {

    private String regex;

    private boolean trim = true;

    private boolean filter = true;

    private boolean split = false;

    private String separator = "；|;|，|,|#| |、|/|\\\\|\\|";

    private int[] groups = new int[]{0};

    private String format = "%s";

    public DenoiserProperties() {
    }

    public DenoiserProperties(String regex) {
        this.regex = regex;
    }

    @Override
    public void validate() throws ValidateException {

    }
}
