package com.octopus.core.properties.collector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/19
 */
@Data
public class AbstractColumnMappingCollectorProperties implements Validatable {

    private List<ColumnMappingProperties> mappings;

    public AbstractColumnMappingCollectorProperties() {
    }

    public AbstractColumnMappingCollectorProperties(List<ColumnMappingProperties> mappings) {
        this.mappings = mappings;
    }

    @Override
    public void validate() throws ValidateException {
        Validator.notEmpty(mappings, "column mappings is required");
        Validator.validateWhenNotNull(this.mappings);
        for (Map.Entry<String, Long> entry : this.mappings.stream().collect(Collectors.groupingBy(ColumnMappingProperties::getColumnName, Collectors.counting()))
                .entrySet()) {
            if (entry.getValue() > 1) {
                throw new ValidateException("same column name [" + entry.getKey() + "]found on mapping");
            }
        }
    }


    @Data
    public static class ColumnMappingProperties implements Validatable {

        private String jsonPath;

        private String columnName;

        public ColumnMappingProperties() {
        }

        public ColumnMappingProperties(@NonNull String columnName, @NonNull String jsonPath) {
            this.jsonPath = jsonPath;
            this.columnName = columnName;
        }

        @Override
        public void validate() throws ValidateException {
            Validator.notBlank(columnName, "mapping column name is required");
            Validator.notBlank(jsonPath, "mapping json path is required");
        }
    }
}
