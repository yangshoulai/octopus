package com.octopus.core.properties.collector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.properties.SelectorProperties;
import com.octopus.core.properties.selector.UrlSelectorProperties;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

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
    @NonNull
    private SelectorProperties name = DEFAULT_NAME_SELECTOR;

    public DownloaderCollectorProperties() {
    }


    @Override
    public void validate() throws ValidateException {
        super.validate();
        Validator.notEmpty(dirs, "download file dirs is required");
        Validator.notEmpty(name, "download file name is required");
    }
}
