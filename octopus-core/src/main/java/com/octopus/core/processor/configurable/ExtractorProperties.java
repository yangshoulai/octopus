package com.octopus.core.processor.configurable;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class ExtractorProperties implements Validator {

    private List<LinkProperties> links = new ArrayList<>();

    private List<FieldProperties> fields = new ArrayList<>();

    public ExtractorProperties() {
    }

    public ExtractorProperties(List<LinkProperties> links, List<FieldProperties> fields) {
        this.links = links;
        this.fields = fields;
    }

    @Override
    public void validate() throws ValidateException {
        if (links != null) {
            for (LinkProperties link : links) {
                link.validate();
            }
        }
        if (fields != null) {
            for (FieldProperties field : fields) {
                field.validate();
            }
        }
    }
}
