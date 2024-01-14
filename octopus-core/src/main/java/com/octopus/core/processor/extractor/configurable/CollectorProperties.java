package com.octopus.core.processor.extractor.configurable;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.extractor.Collector;
import com.octopus.core.processor.extractor.collector.DownloadCollector;
import com.octopus.core.processor.extractor.collector.LoggingCollector;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/14
 */
@Data
public class CollectorProperties implements Validator {

    public static final String DEFAULT_FILE_DIR_PROP = "_file_dir";

    public static final String DEFAULT_FILE_NAME_PROP = "_file_name";

    private CollectorType type = CollectorType.Logging;

    private boolean processResult = true;

    private String downloadDir;

    private String downloadFileDirProp = DEFAULT_FILE_DIR_PROP;

    private String downloadFileNameProp = DEFAULT_FILE_NAME_PROP;


    public Collector<Map<String, Object>> toCollector() {
        switch (type) {
            case Logging:
                return new LoggingCollector<>(processResult);
            case Download:
                return new DownloadCollector<>(downloadDir, downloadFileDirProp, downloadFileNameProp, processResult);
            default:
                return null;
        }
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
}
