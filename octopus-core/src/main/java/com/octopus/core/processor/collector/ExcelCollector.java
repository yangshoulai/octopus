package com.octopus.core.processor.collector;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.octopus.core.Response;
import com.octopus.core.properties.collector.ExcelCollectorProperties;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/19
 */
public class ExcelCollector extends AbstractColumnMappingCollector {

    private final String file;

    private final Lock lock = new ReentrantLock();

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
        lock.lock();
        try {
            try (ExcelWriter writer = ExcelUtil.getWriter(this.file)) {
                writer.setCurrentRowToEnd();
                writer.write(rows);
                writer.autoSizeColumnAll();
            }
        } finally {
            lock.unlock();
        }
    }
}
