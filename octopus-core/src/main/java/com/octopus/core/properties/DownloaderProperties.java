package com.octopus.core.properties;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DownloaderProperties extends DownloadProperties {

    private DownloaderType type = DownloaderType.OkHttp;

    public DownloaderProperties() {
    }

    public DownloaderProperties(DownloaderType type) {
        this.type = type;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notEmpty(type, "downloader type is required");
    }
}
