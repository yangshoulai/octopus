package com.octopus.core.configuration;

import com.octopus.core.WebSite;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class WebSiteProperties implements Validator {

    private String host;

    private Double limitInSecond;

    private DownloadProperties downloadConfig;

    public WebSiteProperties() {
    }

    public WebSiteProperties(String host) {
        this.host = host;
    }

    public WebSite toWebSite() {
        WebSite s = WebSite.of(host);
        if (limitInSecond != null) {
            if (limitInSecond > 1) {
                s.setRateLimiter(limitInSecond.intValue(), 1);
            } else {
                s.setRateLimiter(1, (int) (1 / limitInSecond));
            }
        }
        if (this.downloadConfig != null) {
            s.setDownloadConfig(downloadConfig.toDownloadConfig());
        }

        return s;
    }

    @Override
    public void validate() throws ValidateException {
        if (downloadConfig != null) {
            downloadConfig.validate();
        }
        if (limitInSecond != null && limitInSecond <= 0) {
            throw new ValidateException("site limitInSeconds must be greater than 0");
        }
    }
}
