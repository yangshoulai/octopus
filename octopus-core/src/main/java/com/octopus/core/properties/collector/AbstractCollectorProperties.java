package com.octopus.core.properties.collector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/18
 */
@Data
public class AbstractCollectorProperties implements Validatable {

    private CollectorTarget target = CollectorTarget.Result;

    private boolean pretty = true;

    public AbstractCollectorProperties() {
    }

    public AbstractCollectorProperties(@NonNull CollectorTarget target) {
        this.target = target;
    }

    @Override
    public void validate() throws ValidateException {
        Validator.notEmpty(target, "collector target is empty");
    }
}
