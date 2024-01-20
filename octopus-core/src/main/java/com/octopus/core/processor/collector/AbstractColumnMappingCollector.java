package com.octopus.core.processor.collector;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.octopus.core.Response;
import com.octopus.core.processor.Collector;
import com.octopus.core.properties.collector.ColumnMappingProperties;
import lombok.NonNull;
import net.minidev.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/19
 */
public abstract class AbstractColumnMappingCollector<C extends ColumnMappingProperties> implements Collector<Map<String, Object>> {
    public static final Configuration LIST_CONFIGURATION = Configuration.builder()
            .options(Option.ALWAYS_RETURN_LIST)
            .options(Option.SUPPRESS_EXCEPTIONS)
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build();

    public static final Configuration CONFIGURATION = Configuration.builder()
            .options(Option.SUPPRESS_EXCEPTIONS)
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build();


    protected final List<C> mappings;

    protected final String rowJsonPath;

    public AbstractColumnMappingCollector(@NonNull String rowJsonPath, @NonNull List<C> mappings) {
        this.rowJsonPath = rowJsonPath;
        this.mappings = mappings;
    }

    @Override
    public void collect(Map<String, Object> result, Response response) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (result != null) {
            JSONArray array = JsonPath.using(LIST_CONFIGURATION).parse(result).read(this.rowJsonPath);
            int size = array.size();
            for (int i = 0; i < size; i++) {
                Map<String, Object> row = new HashMap<>();
                Object item = array.get(i);
                if (item != null) {
                    DocumentContext context = JsonPath.using(CONFIGURATION).parse(item);
                    for (C mapping : mappings) {
                        Object columnValue = context.read(mapping.getJsonPath());
                        row.put(mapping.getColumnName(), columnValue);
                    }
                }
                rows.add(row);
            }
        }
        this.collectRows(rows, response);
    }

    public abstract void collectRows(List<Map<String, Object>> rows, Response response);

    protected Object translateColumnValue(String columnName, Object columnValue) {
        if (columnValue == null) {
            return null;
        }
        C mapping = getMappingByColumnName(columnName);
        if (mapping != null && mapping.getTrans() != null && mapping.getTrans().containsKey(columnValue.toString())) {
            return mapping.getTrans().get(columnValue.toString());
        }
        return columnValue;
    }

    protected C getMappingByColumnName(String columnName) {
        return mappings.stream().filter(c -> columnName.equals(c.getColumnName())).findFirst().orElse(null);
    }
}
