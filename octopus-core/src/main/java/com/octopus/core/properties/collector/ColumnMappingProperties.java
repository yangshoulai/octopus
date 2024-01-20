package com.octopus.core.properties.collector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/20
 */
@Data
public class ColumnMappingProperties implements Validatable {

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
