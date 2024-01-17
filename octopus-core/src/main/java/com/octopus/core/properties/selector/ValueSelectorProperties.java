package com.octopus.core.properties.selector;

import com.octopus.core.exception.ValidateException;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ValueSelectorProperties extends AbstractSelectorProperties {

    private String value;

    public ValueSelectorProperties() {
    }

    public ValueSelectorProperties(String value) {
        this.value = value;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
    }
}
