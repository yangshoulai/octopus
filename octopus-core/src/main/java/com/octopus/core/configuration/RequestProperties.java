package com.octopus.core.configuration;

/**
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

    private String url;

    private Request.RequestMethod method = Request.RequestMethod.GET;

    private Map<String, String> params = new HashMap<>();

    private Map<String, String> headers = new HashMap<>();

    private int priority;

    private boolean repeatable = true;

    private Map<String, Object> attributes = new HashMap<>();

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
