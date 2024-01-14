package com.octopus.core.configuration;

/**
 * 请求配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RequestProperties implements Validator {

    /**
     * 请求 URL
     * <p>
     * 默认 空
     */
    private String url;

    /**
     * 请求方法
     * <p>
     * 默认 GET
     */
    private Request.RequestMethod method = Request.RequestMethod.GET;

    /**
     * 请求参数
     * <p>
     * 默认 空
     */
    private Map<String, String> params = new HashMap<>();

    /**
     * 请求头
     * <p>
     * 默认 空
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 请求优先级
     * <p>
     * 默认 0
     */
    private int priority;

    /**
     * 是否重复请求
     * <p>
     * 默认 true
     */
    private boolean repeatable = true;

    /**
     * 请求属性
     * <p>
     * 默认 空
     */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 是否继承父节点的属性
     * <p>
     * 默认 false
     */
    private boolean inherit = false;

    public RequestProperties() {

    }

    public RequestProperties(String url) {
        this.url = url;
    }

    public RequestProperties(String url, Request.RequestMethod method) {
        this.url = url;
        this.method = method;
    }

    public Request toRequest() {
        Request r = new Request(url, method);
        r.setParams(params);
        r.setHeaders(headers);
        r.setPriority(priority);
        r.setAttributes(attributes);
        r.setInherit(inherit);
        r.setRepeatable(repeatable);
        return r;
    }


    @Override
    public void validate() throws ValidateException {
        if (StrUtil.isBlank(url)) {
            throw new ValidateException("request url is required");
        }
        if (method == null) {
            throw new ValidateException("request method is required");
        }
    }
}
