package com.octopus.core.properties.selector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RegexSelectorProperties extends AbstractSelectorProperties {

    private String expression;

    private int[] groups = new int[]{0};

    private String format = "%s";

    public RegexSelectorProperties() {
    }

    public RegexSelectorProperties(@NonNull String expression) {
        this.expression = expression;
    }

    public RegexSelectorProperties(String expression, int[] groups, String format) {
        this.expression = expression;
        this.groups = groups;
        this.format = format;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notBlank(expression, "regex selector expression is required");
    }
}
