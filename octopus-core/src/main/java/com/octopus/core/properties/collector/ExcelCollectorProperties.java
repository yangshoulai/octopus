package com.octopus.core.properties.collector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/19
 */
@Data
public class ExcelCollectorProperties extends AbstractColumnMappingCollectorProperties implements Validatable {

    private List<ExcelColumnMappingProperties> mappings;

    private boolean append = true;

    private String file;

    public ExcelCollectorProperties() {
    }

    public ExcelCollectorProperties(@NonNull List<ExcelColumnMappingProperties> mappings, @NonNull String file) {
        this(mappings, file, true);
    }

    public ExcelCollectorProperties(@NonNull List<ExcelColumnMappingProperties> mappings, @NonNull String file, boolean append) {
        this.mappings = mappings;
        this.append = append;
        this.file = file;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate(mappings);
        Validator.notBlank(file, "excel collector file name is required");
    }

}
