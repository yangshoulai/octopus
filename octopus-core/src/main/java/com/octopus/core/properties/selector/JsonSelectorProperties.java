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
public class JsonSelectorProperties extends AbstractSelectorProperties {

    private String expression;


    public JsonSelectorProperties() {
    }

    public JsonSelectorProperties(@NonNull String expression) {
        this.expression = expression;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notBlank(expression, "json selector expression is required");
    }
}
