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
public class XpathSelectorProperties extends AbstractSelectorProperties {

    private String expression;

    private boolean node = true;


    public XpathSelectorProperties() {
    }

    public XpathSelectorProperties(@NonNull String expression) {
        this.expression = expression;
    }

    public XpathSelectorProperties(@NonNull String expression, boolean node) {
        this.expression = expression;
        this.node = node;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notBlank(expression, "xpath selector expression is required");
    }
}
