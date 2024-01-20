package com.octopus.core.processor.collector;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.octopus.core.Response;
import com.octopus.core.properties.collector.ExcelCollectorProperties;
import com.octopus.core.properties.collector.ExcelColumnMappingProperties;
import lombok.NonNull;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/19
 */
public class ExcelCollector extends AbstractColumnMappingCollector<ExcelColumnMappingProperties> {
    static {
        ZipSecureFile.setMinInflateRatio(0.001);
    }

    private final String file;

    private final Lock lock = new ReentrantLock();

    private final Map<String, Integer> index = new HashMap<>();

    private final Comparator<String> comparator;

    public ExcelCollector(@NonNull ExcelCollectorProperties properties) {
        super(properties.getMappings());
        boolean append = properties.isAppend();
        this.file = properties.getFile();
        if (!append) {
            FileUtil.del(this.file);
        }
        for (int i = 0; i < properties.getMappings().size(); i++) {
            index.put(properties.getMappings().get(i).getColumnName(), i);
        }
        this.comparator = (o1, o2) -> {
            int i1 = index.getOrDefault(o1, 0);
            int i2 = index.getOrDefault(o2, 0);
            return i1 - i2;
        };
        try (ExcelWriter writer = ExcelUtil.getWriter(this.file)) {
            for (int i = 0; i < mappings.size(); i++) {
                writer.setColumnWidth(i, mappings.get(i).getWidth());
            }
        }
    }

    @Override
    public void collectRows(List<Map<String, Object>> rows, Response response) {
        lock.lock();
        try {
            try (ExcelWriter writer = ExcelUtil.getWriter(this.file)) {
                writer.setCurrentRowToEnd();
                int beginRow = writer.getCurrentRow();
                if (writer.getCurrentRow() == 0) {
                    writer.write(rows, comparator);
                } else {
                    writer.write(rows.stream().map(m -> {
                        Map<String, Object> treeMap = new TreeMap<>(comparator);
                        treeMap.putAll(m);
                        return treeMap;
                    }).collect(Collectors.toList()), false);
                }
                int endRow = writer.getCurrentRow();

                for (int i = 0; i < mappings.size(); i++) {
                    ExcelColumnMappingProperties style = mappings.get(i);
                    if (endRow - beginRow > 0) {
                        for (int j = beginRow; j < endRow; j++) {
                            Cell cell = writer.getCell(i, j);
                            CellStyle cellStyle = cell.getCellStyle();
                            cellStyle.setWrapText(style.isWrap());
                            if (style.getAlign() != null) {
                                cellStyle.setAlignment(style.getAlign());
                            }
                            if (!StrUtil.isBlank(style.getFormat())) {
                                DataFormat f = writer.getWorkbook().getCreationHelper().createDataFormat();
                                cellStyle.setDataFormat(f.getFormat(style.getFormat()));
                            } else {
                                cellStyle.setDataFormat((short) 0);
                            }
                            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                            cell.setCellStyle(cellStyle);
                        }
                    }
                    if (style.isAutoSize()) {
                        writer.autoSizeColumn(i);
                    }
                }

            }
        } finally {
            lock.unlock();
        }
    }
}
