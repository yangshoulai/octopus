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
public class CssSelectorProperties extends AbstractSelectorProperties {

    private String expression;

    private boolean self = false;

    private String attr;

    public CssSelectorProperties() {
    }

    public CssSelectorProperties(@NonNull String expression) {
        this.expression = expression;
    }

    public CssSelectorProperties(@NonNull String expression, boolean self, String attr) {
        this.expression = expression;
        this.self = self;
        this.attr = attr;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notBlank(expression, "css selector expression is required");
    }
}
