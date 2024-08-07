package com.octopus.core.properties.collector;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.Collector;
import com.octopus.core.processor.collector.*;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

    private ExcelCollectorProperties excel;

    private MongodbCollectorProperties mongodb;

    private CustomCollectorProperties custom;


    public CollectorProperties() {
    }

    @Override
    public void validate() throws ValidateException {
        Validator.validateWhenNotNull(logging);
        Validator.validateWhenNotNull(download);
        Validator.validateWhenNotNull(excel);
        Validator.validateWhenNotNull(mongodb);
        Validator.validateWhenNotNull(custom);
    }

    @Override
    public Collector<Map<String, Object>> transform() {
        List<Collector<Map<String, Object>>> collectors = new ArrayList<>();
        if (this.logging != null) {
            collectors.add(new LoggingCollector<>(this.logging));
        }
        if (this.download != null) {
            collectors.add(new DownloadCollector<>(this.download));
        }
        if (this.excel != null) {
            collectors.add(new ExcelCollector(this.excel));
        }
        if (this.mongodb != null) {
            collectors.add(new MongodbCollector<>(this.mongodb));
        }
        if (this.custom != null) {
            Class<? extends AbstractCustomCollector> cls = ClassUtil.loadClass(custom.getCollector());
            AbstractCustomCollector collector = null;
            try {
                collector = ReflectUtil.getConstructor(cls, Properties.class).newInstance(custom.getConf());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            collectors.add(collector);
        }
        return (result, response) -> {
            for (Collector<Map<String, Object>> collector : collectors) {
                collector.collect(result, response);
            }
        };
    }
}
