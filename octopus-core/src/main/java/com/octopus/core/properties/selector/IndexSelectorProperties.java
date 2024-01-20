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
public class IndexSelectorProperties extends AbstractSelectorProperties {

    @Override
    public void validate() throws ValidateException {
        super.validate();
    }
}
