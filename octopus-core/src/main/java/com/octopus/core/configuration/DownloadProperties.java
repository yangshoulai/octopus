package com.octopus.core.configuration;

import cn.hutool.core.util.CharsetUtil;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.proxy.PollingProxyProvider;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class DownloadProperties implements Validator {

    private int timeoutInSeconds = 60;

    private String charset = CharsetUtil.CHARSET_UTF_8.name();

    private Map<String, String> headers = new HashMap<String, String>() {{
        put("User-Agent", DownloadConfig.DEFAULT_UA);
        put("Accept", DownloadConfig.DEFAULT_ACCEPT);
    }};

    private List<ProxyProperties> proxies = new ArrayList<>();

    public DownloadConfig toDownloadConfig() {
        DownloadConfig config = new DownloadConfig();
        config.setConnectTimeout(getTimeoutInSeconds() * 1000);
        config.setSocketTimeout(getTimeoutInSeconds() * 1000);

        config.setCharset(Charset.forName(charset));
        config.setHeaders(getHeaders());
        if (getProxies() != null) {
            config.setProxyProvider(new PollingProxyProvider(getProxies().stream().map(ProxyProperties::toProxy).collect(Collectors.toList())));
        }
        return config;
    }

    @Override
    public void validate() throws ValidateException {
        if (proxies != null) {
            for (ProxyProperties proxy : proxies) {
                proxy.validate();
            }
        }
    }
}
