package com.octopus.core.processor.collector;

import com.octopus.core.processor.Collector;

import java.util.Map;
import java.util.Properties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/19
 */
public abstract class AbstractCustomCollector implements Collector<Map<String, Object>> {

    protected final Properties conf;

    public AbstractCustomCollector(Properties conf) {
        this.conf = conf;
    }


}
