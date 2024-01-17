package com.octopus.core.properties.selector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validatable;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/17
 */
@Data
public class AbstractSelectorProperties implements Validatable {

    private DenoiserProperties denoiser = new DenoiserProperties();

    private String defaultValue;

    @Override
    public void validate() throws ValidateException {
        if (denoiser != null) {
            denoiser.validate();
        }
    }
}
