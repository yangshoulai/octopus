package com.octopus.core.configuration;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.WebSite;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.proxy.PollingProxyProvider;
import com.octopus.core.exception.ValidateException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WebSiteProperties extends DownloadProperties {

    private String host;

    private Integer limitInSeconds;

    public WebSiteProperties() {
    }

    public WebSiteProperties(String host) {
        this.host = host;
    }

    public WebSite toWebSite() {
        WebSite s = WebSite.of(host);
        if (limitInSeconds != null) {
            s.setRateLimiter(limitInSeconds, 1);
        }
        s.setDownloadConfig(toDownloadConfig());
        return s;
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        if (StrUtil.isBlank(host)) {
            throw new ValidateException("site host is required");
        }
        if (limitInSeconds != null && limitInSeconds <= 0) {
            throw new ValidateException("site limitInSeconds must be greater than 0");
        }
    }
}
