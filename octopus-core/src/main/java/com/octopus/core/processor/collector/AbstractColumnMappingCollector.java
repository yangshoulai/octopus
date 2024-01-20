package com.octopus.core.processor.collector;

import cn.hutool.core.lang.Pair;
import com.jayway.jsonpath.Configuration;
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
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/19
 */
public abstract class AbstractColumnMappingCollector<C extends ColumnMappingProperties> implements Collector<Map<String, Object>> {
    public static final Configuration CONFIGURATION = Configuration.builder()
            .options(Option.ALWAYS_RETURN_LIST)
            .options(Option.SUPPRESS_EXCEPTIONS)
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build();


    protected final List<C> mappings;

    public AbstractColumnMappingCollector(@NonNull List<C> mappings) {
        this.mappings = mappings;
    }

    @Override
    public void collect(Map<String, Object> result, Response response) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (result != null) {
            List<Pair<String, JSONArray>> results = mappings.stream().map(m -> Pair.of(m.getColumnName(), JsonPath.using(CONFIGURATION).parse(result).<JSONArray>read(m.getJsonPath())))
                    .collect(Collectors.toList());
            int size = results.stream().map(a -> a.getValue() == null ? 0 : a.getValue().size()).max(Integer::compareTo).orElse(0);
            for (int i = 0; i < size; i++) {
                Map<String, Object> row = new HashMap<>();
                for (Pair<String, JSONArray> pair : results) {
                    Object columnValue = pair.getValue() == null || pair.getValue().size() <= i ? null : pair.getValue().get(i);
                    row.put(pair.getKey(), translateColumnValue(pair.getKey(), columnValue));
                }
                rows.add(row);
            }
        }
        this.collectRows(rows, response);
    }

    public abstract void collectRows(List<Map<String, Object>> rows, Response response);

    private Object translateColumnValue(String columnName, Object columnValue) {
        if (columnValue == null) {
            return null;
        }
        C mapping = mappings.stream().filter(m -> m.getColumnName().equals(columnName)).findFirst().orElse(null);
        if (mapping != null && mapping.getTrans() != null && mapping.getTrans().containsKey(columnValue.toString())) {
            return mapping.getTrans().get(columnValue.toString());
        }
        return columnValue;
    }
}
