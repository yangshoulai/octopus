package com.octopus.core.configurable;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.extractor.Collector;
import com.octopus.core.processor.extractor.collector.DownloadCollector;
import com.octopus.core.processor.extractor.collector.LoggingCollector;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

/**
 * 收集器配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/14
 */
@Data
public class CollectorProperties implements Validatable, Transformable<Collector<Map<String, Object>>> {

    public static final String DEFAULT_FILE_DIR_PROP = "_file_dir";

    public static final String DEFAULT_FILE_NAME_PROP = "_file_name";

    /**
     * 收集器类型
     * <p>
     * 默认 Logging
     */
    private CollectorType type = CollectorType.Logging;

    /**
     * 是否搜集结果（
     * true 为收集结果 false 为收集响应体
     * <p>
     * 默认 true
     */
    private boolean collectResult = true;

    /**
     * 下载搜集器 下载目录
     * <p>
     * 默认 空
     */
    private String downloadDir;

    /**
     * 下载搜集器 下载分类目录属性名
     * <p>
     * 默认 _file_dir
     */
    private String downloadFileDirProp = DEFAULT_FILE_DIR_PROP;

    /**
     * 下载搜集器 下载文件属性名
     * <p>
     * 默认 _file_name
     */
    private String downloadFileNameProp = DEFAULT_FILE_NAME_PROP;

    public CollectorProperties() {
    }

    public CollectorProperties(CollectorType type) {
        this.type = type;
    }

    @Override
    public void validate() throws ValidateException {
        if (type == null) {
            throw new ValidateException("collector type is null");
        }

        if (type == CollectorType.Download) {
            if (StrUtil.isBlank(downloadDir)) {
                throw new ValidateException("download collector dir is null");
            }
        }
    }

    @Override
    public Collector<Map<String, Object>> transform() {
        switch (type) {
            case Logging:
                return new LoggingCollector<>(collectResult);
            case Download:
                return new DownloadCollector<>(downloadDir, downloadFileDirProp, downloadFileNameProp, collectResult);
            default:
                return null;
        }
    }
}
