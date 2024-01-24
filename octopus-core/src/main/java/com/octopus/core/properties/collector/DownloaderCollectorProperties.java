package com.octopus.core.properties.collector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.properties.selector.SelectorProperties;
import com.octopus.core.properties.selector.UrlSelectorProperties;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DownloaderCollectorProperties extends AbstractCollectorProperties {

    public static final SelectorProperties DEFAULT_NAME_SELECTOR = new SelectorProperties();

    static {
        DEFAULT_NAME_SELECTOR.setUrl(new UrlSelectorProperties());
        DEFAULT_NAME_SELECTOR.getDenoiser().setRegex("^.*/([^/\\?]+)[^/]*$");
        DEFAULT_NAME_SELECTOR.getDenoiser().setGroups(new int[]{1});
    }


    /**
     * 下载文件目录
     */
    private String dir;

    /**
     * 下载文件名称
     */
    private String name;

    public DownloaderCollectorProperties() {
    }

    public DownloaderCollectorProperties(String dir) {
        super();
        this.dir = dir;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notEmpty(dir, "download dir is required");
    }
}
