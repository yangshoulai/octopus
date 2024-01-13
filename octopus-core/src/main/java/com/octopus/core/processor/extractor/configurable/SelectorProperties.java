package com.octopus.core.processor.extractor.configurable;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.extractor.annotation.Selector;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class SelectorProperties implements Validator {

    private Selector.Type type = Selector.Type.None;

    private String value;

    private String def;

    private String attr;

    private boolean self = false;

    private int[] groups = new int[]{0};

    private String format = "%s";

    private boolean node = false;

    private FormatterProperties formatter = null;

    public SelectorProperties() {
    }

    public SelectorProperties(Selector.Type type) {
        this.type = type;
    }

    public SelectorProperties(Selector.Type type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public void validate() throws ValidateException {
        if (type == null) {
            throw new ValidateException("selector type is required");
        }
        if (formatter != null) {
            formatter.validate();
        }
    }
}
