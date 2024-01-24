package com.octopus.core.processor.jexl;

import com.octopus.core.Request;
import com.octopus.core.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/24
 */
public class JexlHelper {

    public static Map<String, Object> buildContext(Object result, Request request, Response response) {
        if (request == null && response != null) {
            request = response.getRequest();
        }
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("result", request);
        ctx.put("request", request);
        ctx.put("response", response);
        return ctx;
    }

    public static Map<String, Object> buildContext(Object result, Response response) {
        return buildContext(result, response.getRequest(), response);
    }
}
