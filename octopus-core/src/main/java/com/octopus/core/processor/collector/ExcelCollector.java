package com.octopus.core.processor.collector;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.octopus.core.Response;
import com.octopus.core.properties.collector.ExcelCollectorProperties;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/19
 */
public class ExcelCollector extends AbstractColumnMappingCollector {

    private final String file;

    public ExcelCollector(@NonNull ExcelCollectorProperties properties) {
        super(properties);
        boolean append = properties.isAppend();
        this.file = properties.getFile();
        if (!append) {
            FileUtil.del(this.file);
        }
    }

    @Override
    public void collectRows(List<Map<String, Object>> rows, Response response) {
        try (ExcelWriter writer = ExcelUtil.getWriter(this.file)) {
            writer.setCurrentRowToEnd();
            writer.write(rows);
            writer.autoSizeColumnAll();
        }
    }
}
