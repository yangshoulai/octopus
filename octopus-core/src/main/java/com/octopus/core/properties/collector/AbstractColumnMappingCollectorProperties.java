package com.octopus.core.properties.collector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/19
 */
@Data
public class AbstractColumnMappingCollectorProperties {

    protected String rowJsonPath;

    public <C extends ColumnMappingProperties> void validate(List<C> mappings) throws ValidateException {
        Validator.notEmpty(mappings, "column mappings is required");
        Validator.validateWhenNotNull(mappings);
        for (Map.Entry<String, Long> entry : mappings.stream().collect(Collectors.groupingBy(ColumnMappingProperties::getColumnName, Collectors.counting()))
                .entrySet()) {
            if (entry.getValue() > 1) {
                throw new ValidateException("same column name [" + entry.getKey() + "]found on mapping");
            }
        }
    }


}
