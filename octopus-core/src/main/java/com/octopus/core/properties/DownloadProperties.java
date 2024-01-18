package com.octopus.core.properties;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.Header;
import com.octopus.core.downloader.DownloadConfig;
import com.octopus.core.downloader.proxy.PollingProxyProvider;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 下载配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class DownloadProperties implements Validatable, Transformable<DownloadConfig> {

    /**
     * 下载超时时间（单位秒）
     * <p>
     * 默认 60s
     */
    private int timeoutInSeconds = 60;

    /**
     * 默认字符集
     * <p>
     * 默认 UTF-8
     */
    private String charset = CharsetUtil.CHARSET_UTF_8.name();

    /**
     * 默认请求头
     * <p>
     * 默认 User-Agent,Accept
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 代理配置
     * <p>
     * 默认 空
     */
    private List<ProxyProperties> proxies = new ArrayList<>();

    @Override
    public void validate() throws ValidateException {
        Validator.validateWhenNotNull(proxies);
    }

    @Override
    public DownloadConfig transform() {
        DownloadConfig config = new DownloadConfig();
        config.setConnectTimeout(getTimeoutInSeconds() * 1000);
        config.setSocketTimeout(getTimeoutInSeconds() * 1000);
        config.setCharset(Charset.forName(charset));
        Map<String, String> h = getHeaders();
        if (!h.containsKey(Header.USER_AGENT.getValue())) {
            h.put(Header.USER_AGENT.getValue(), DownloadConfig.DEFAULT_UA);
        }
        if (!h.containsKey(Header.ACCEPT.getValue())) {
            h.put(Header.ACCEPT.getValue(), DownloadConfig.DEFAULT_ACCEPT);
        }
        config.setHeaders(h);
        if (getProxies() != null) {
            config.setProxyProvider(new PollingProxyProvider(getProxies().stream().map(ProxyProperties::transform).collect(Collectors.toList())));
        }
        return config;
    }
}
