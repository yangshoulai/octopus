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
public class ParamSelectorProperties extends AbstractSelectorProperties {

    private String name;

    public ParamSelectorProperties() {
    }

    public ParamSelectorProperties(@NonNull String name) {
        this.name = name;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notBlank(name, "param selector name is required");
    }
}
