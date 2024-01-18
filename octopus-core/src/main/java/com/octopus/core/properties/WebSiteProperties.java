package com.octopus.core.properties;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.WebSite;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * 站点配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class WebSiteProperties implements Validatable, Transformable<WebSite> {

    /**
     * 站点域名
     * <p>
     * 默认 空
     */
    private String host;

    /**
     * 站点限速 每秒多少个请求
     * 可配置为小数
     * <p>
     * 默认 空（不限速）
     */
    private Double limitInSecond;

    /**
     * 站点下载配置
     * <p>
     * 默认 空
     */
    private DownloadProperties downloadConfig;

    public WebSiteProperties() {
    }

    public WebSiteProperties(String host) {
        this.host = host;
    }


    @Override
    public void validate() throws ValidateException {
        Validator.notBlank(host, "site host is required");
        Validator.validateWhenNotNull(downloadConfig);
        if (limitInSecond != null && limitInSecond <= 0) {
            Validator.gt(limitInSecond, 0d, "site limitInSeconds must be greater than 0");
        }
    }

    @Override
    public WebSite transform() {
        WebSite s = WebSite.of(host);
        if (limitInSecond != null) {
            if (limitInSecond > 1) {
                s.setRateLimiter(limitInSecond.intValue(), 1);
            } else {
                s.setRateLimiter(1, (int) (1 / limitInSecond));
            }
        }
        if (this.downloadConfig != null) {
            s.setDownloadConfig(downloadConfig.transform());
        }
        return s;
    }
}
