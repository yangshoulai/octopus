package com.octopus.core.properties.collector;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExcelColumnMappingProperties extends ColumnMappingProperties {

    private boolean autoSize = true;

    private boolean wrap = true;

    private int width = 10;

    private String format;

    private HorizontalAlignment align = HorizontalAlignment.LEFT;
}
