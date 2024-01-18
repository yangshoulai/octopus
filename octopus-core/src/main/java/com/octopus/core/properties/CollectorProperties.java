package com.octopus.core.properties;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.Collector;
import com.octopus.core.processor.collector.DownloadCollector;
import com.octopus.core.processor.collector.LoggingCollector;
import com.octopus.core.properties.collector.DownloaderCollectorProperties;
import com.octopus.core.properties.collector.LoggingCollectorProperties;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.Map;

/**
 * 收集器配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/14
 */
@Data
public class CollectorProperties implements Validatable, Transformable<Collector<Map<String, Object>>> {

    private LoggingCollectorProperties logging;

    private DownloaderCollectorProperties download;


    public CollectorProperties() {
    }

    @Override
    public void validate() throws ValidateException {
        Validator.validateWhenNotNull(logging);
        Validator.validateWhenNotNull(download);
    }

    @Override
    public Collector<Map<String, Object>> transform() {
        return (result, response) -> {
            if (this.logging != null) {
                new LoggingCollector<>(this.logging).collect(result, response);
            }
            if (this.download != null) {
                new DownloadCollector<>(this.download).collect(result, response);
            }
        };
    }
}
