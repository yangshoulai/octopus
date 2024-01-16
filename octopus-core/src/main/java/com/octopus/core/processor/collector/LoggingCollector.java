package com.octopus.core.processor.collector;

import cn.hutool.json.JSONUtil;
import com.octopus.core.Response;
import com.octopus.core.configurable.CollectorProperties;
import com.octopus.core.configurable.CollectorTarget;
import com.octopus.core.logging.Logger;
import com.octopus.core.logging.LoggerFactory;
import com.octopus.core.processor.Collector;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/14
 */
public class LoggingCollector<R> implements Collector<R> {
    private final Logger logger = LoggerFactory.getLogger(LoggingCollector.class);

    private final CollectorProperties properties;

    public LoggingCollector(CollectorProperties properties) {
        this.properties = properties;
    }


    @Override
    public void collect(R result, Response response) {
        if (properties.getTarget() == CollectorTarget.Body) {
            logger.info(response.asText());
        } else {
            if (this.properties.isPretty()) {
                logger.info(JSONUtil.toJsonPrettyStr(result));
            } else {
                logger.info(JSONUtil.toJsonStr(result));
            }
        }
    }

}
