package com.octopus.core.processor.collector;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.octopus.core.Response;
import com.octopus.core.processor.jexl.Jexl;
import com.octopus.core.properties.collector.ExcelCollectorProperties;
import com.octopus.core.properties.collector.ExcelColumnMappingProperties;
import lombok.NonNull;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        super(properties.getRowJsonPath(), properties.getMappings());
        boolean append = properties.isAppend();
        Object file = Jexl.eval(properties.getFile());
        this.file = file == null ? properties.getFile() : file.toString();
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
                ExcelColumnMappingProperties style = mappings.get(i);
                writer.setColumnWidth(i, style.getWidth());
            }
        }
    }

    @Override
    public void collectRows(List<Map<String, Object>> rows, Response response) {
        lock.lock();
        try {
            rows = rows.stream().map(this::convertRow).collect(Collectors.toList());
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
                            CellStyle origin = cell.getCellStyle();
                            CellStyle cellStyle = writer.createCellStyle(i, j);
                            cellStyle.cloneStyleFrom(origin);
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
                            // cell.setCellStyle(cellStyle);
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

    private Map<String, Object> convertRow(Map<String, Object> row) {
        Map<String, Object> newRow = new HashMap<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String columnName = entry.getKey();
            Object columnValue = entry.getValue();
            ExcelColumnMappingProperties mapping = getMappingByColumnName(columnName);
            if (columnValue != null) {
                Class<?> type = columnValue.getClass();
                Stream<?> stream = null;
                if (ArrayUtil.isArray(type)) {
                    Object[] array = (Object[]) columnValue;
                    stream = Arrays.stream(array);
                } else if (Iterable.class.isAssignableFrom(type)) {
                    Iterable<?> iterable = (Iterable<?>) columnValue;
                    List<Object> list = new ArrayList<>();
                    iterable.forEach(list::add);
                    stream = list.stream();
                }
                if (stream != null) {
                    if (mapping.getDelimiter() != null) {
                        columnValue = stream.map(v -> {
                            if (v == null) {
                                return null;
                            }
                            if (ClassUtil.isBasicType(v.getClass())) {
                                Object val = translateColumnValue(columnName, v.toString());
                                return val == null ? null : val.toString();
                            }
                            return JSONUtil.toJsonStr(v, new JSONConfig().setDateFormat(mapping.getFormat()));

                        }).filter(StrUtil::isNotBlank).collect(Collectors.joining(mapping.getDelimiter()));
                    } else {
                        columnValue = translateColumnValue(columnName, columnValue);
                    }
                } else {
                    columnValue = translateColumnValue(columnName, columnValue);
                }
            }
            newRow.put(entry.getKey(), columnValue);
        }
        return newRow;
    }
}
