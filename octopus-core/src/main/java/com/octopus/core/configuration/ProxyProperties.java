package com.octopus.core.configuration;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.net.Proxy;

/**
 * 代理配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class ProxyProperties implements Validator {

    /**
     * 代理类型
     * <p>
     * 默认 HTTP
     */
    private Proxy.Type type = Proxy.Type.HTTP;

    /**
     * 主机
     * <p>
     * 默认 127.0.0.1
     */
    private String host = "127.0.0.1";

    /**
     * 端口
     * <p>
     * 默认 0
     */
    private int port;

    public Proxy toProxy() {
        return new Proxy(type, new java.net.InetSocketAddress(host, port));
    }

    @Override
    public void validate() throws ValidateException {
        if (type == null) {
            throw new ValidateException("proxy type is required");
        }
        if (StrUtil.isBlank(host)) {
            throw new ValidateException("proxy host is required");
        }
        if (port <= 0) {
            throw new ValidateException("proxy port is invalid");
        }
    }
}
