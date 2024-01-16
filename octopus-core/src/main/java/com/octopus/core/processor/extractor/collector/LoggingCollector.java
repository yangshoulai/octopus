package com.octopus.core.processor.extractor.collector;

import cn.hutool.json.JSONUtil;
import com.octopus.core.Response;
import com.octopus.core.logging.Logger;
import com.octopus.core.logging.LoggerFactory;
import com.octopus.core.processor.extractor.Collector;
import lombok.Getter;
import lombok.Setter;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/14
 */
public class LoggingCollector<R> implements Collector<R> {
    private final Logger logger = LoggerFactory.getLogger(LoggingCollector.class);

    @Getter
    @Setter
    private boolean logResult = true;

    public LoggingCollector() {
    }

    public LoggingCollector(boolean logResult) {
        this.logResult = logResult;
    }

    @Override
    public void collect(R result, Response response) {
        if (!logResult) {
            logger.info(response.asText());
        } else {
            logger.info(JSONUtil.toJsonStr(result));
        }
    }

}
