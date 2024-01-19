package com.octopus.core.properties.collector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/19
 */
@Data
public class ExcelCollectorProperties extends AbstractColumnMappingCollectorProperties {

    private boolean append = true;

    private String file;

    public ExcelCollectorProperties() {
    }

    public ExcelCollectorProperties(@NonNull List<ColumnMappingProperties> mappings, @NonNull String file) {
        this(mappings, file, true);
    }

    public ExcelCollectorProperties(@NonNull List<ColumnMappingProperties> mappings, @NonNull String file, boolean append) {
        super(mappings);
        this.append = append;
        this.file = file;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notBlank(file, "excel collector file name is required");
    }


}
