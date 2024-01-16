package com.octopus.core.configurable;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.Collector;
import com.octopus.core.processor.collector.DownloadCollector;
import com.octopus.core.processor.collector.LoggingCollector;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
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

    /**
     * 收集器类型
     * <p>
     * 默认 Logging
     */
    private CollectorType type = CollectorType.Logging;

    /**
     * 搜集对象
     * <p>
     * 默认 Result
     */
    private CollectorTarget target = CollectorTarget.Result;

    /**
     * 下载搜集器 下载分类目录属性名列表
     * <p>
     * 默认 空
     */
    private SelectorProperties[] dirs = new SelectorProperties[]{};

    /**
     * 下载搜集器 下载文件属性名
     * <p>
     * 默认 空
     */
    private SelectorProperties name;

    /**
     * 是否美化文本内容
     */
    private boolean pretty = true;


    public CollectorProperties() {
    }

    public CollectorProperties(CollectorType type) {
        this.type = type;
    }

    @Override
    public void validate() throws ValidateException {
        if (type == null) {
            throw new ValidateException("collector type is required");
        }

        if (target == null) {
            throw new ValidateException("collector target is required");
        }

        if (type == CollectorType.Download) {
            if (dirs == null || dirs.length == 0) {
                throw new ValidateException("collector directory selectors is required");
            }
        }
        for (SelectorProperties dir : dirs) {
            dir.validate();
        }
        if (name != null) {
            name.validate();
        }
    }

    @Override
    public Collector<Map<String, Object>> transform() {
        switch (type) {
            case Logging:
                return new LoggingCollector<>(this);
            case Download:
                return new DownloadCollector<>(this);
            default:
                return null;
        }
    }
}
