package com.octopus.core.properties;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
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
public class ProxyProperties implements Validatable, Transformable<Proxy> {

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

    @Override
    public void validate() throws ValidateException {
        Validator.notEmpty(type, "proxy type is required");
        Validator.notBlank(host, "proxy host is required");
        Validator.gt(port, 0, "proxy port is invalid");
    }

    @Override
    public Proxy transform() {
        return new Proxy(type, new java.net.InetSocketAddress(host, port));
    }
}
