package com.octopus.core.properties;

import cn.hutool.core.util.ClassUtil;
import com.octopus.core.downloader.AbstractCustomDownloader;
import com.octopus.core.downloader.HttpClientDownloader;
import com.octopus.core.downloader.OkHttpDownloader;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Properties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DownloaderProperties extends DownloadProperties {

    private String type = HttpClientDownloader.class.getName();

    private Properties conf;

    public DownloaderProperties() {
    }

    public String resolveDownloaderClass() {
        if (DownloaderType.OkHttp.name().equalsIgnoreCase(type)) {
            return OkHttpDownloader.class.getName();
        } else if (DownloaderType.HttpClient.name().equalsIgnoreCase(type)) {
            return HttpClientDownloader.class.getName();
        }
        return type;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notEmpty(type, "downloader type is required");
        String clz = resolveDownloaderClass();
        try {
            Class<?> cls = ClassUtil.loadClass(clz);
            if (!AbstractCustomDownloader.class.isAssignableFrom(cls)) {
                throw new ValidateException("class [" + this.type + "] must extends AbstractCustomDownloader");
            }
            if (ClassUtil.isAbstract(cls)) {
                throw new ValidateException("class [" + this.type + "] must not be abstract");
            }
        } catch (Exception e) {
            throw new ValidateException("can not load downloader [" + this.type + "]");
        }
    }
}
