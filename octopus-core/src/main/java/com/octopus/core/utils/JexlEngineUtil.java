package com.octopus.core.utils;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.URLUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/24
 */
public class JexlEngineUtil {
    public static final JexlEngine JEXL_ENGINE = new JexlBuilder().cache(512).strict(true).silent(false).create();

    public static Object eval(String expression, Map<String, Object> context) {
        MapContext ctx = new MapContext(context);
        try {
            return JEXL_ENGINE.createExpression(expression).evaluate(ctx);
        } catch (Exception ignore) {
        }
        try {
            return JEXL_ENGINE.createScript(expression).execute(ctx);
        } catch (Exception ignore) {

        }
        return null;
    }

    public static Map<String, Object> buildContext() {
        Map<String, Object> ctx = new HashMap<>();
        Map<String, Object> env = new HashMap<>();
        env.putAll(System.getenv());
        Properties properties = System.getProperties();
        properties.forEach((k, v) -> env.put(k.toString(), v));
        ctx.put("env", env);
        return ctx;

    }

    public static Map<String, Object> buildContext(Request request) {
        Map<String, Object> ctx = buildContext();
        if (request != null) {
            Map<String, Object> req = new HashMap<>();
            req.put("id", request.getId());
            req.put("url", request.getUrl());
            req.put("method", request.getMethod().name());
            req.put("attrs", request.getAttributes());
            req.put("header", request.getHeaders());
            req.put("failTimes", request.getFailTimes());
            req.put("priority", request.getPriority());
            req.put("repeatable", request.isRepeatable());
            req.put("cache", request.isCache());
            req.put("inherit", request.isInherit());
            req.put("parent", request.getParent());
            req.put("index", request.getIndex());
            req.put("createDate", request.getCreateDate());
            req.put("depth", request.getDepth());
            Map<String, Object> params = new HashMap<>(request.getParams());
            UrlBuilder.of(request.getUrl()).getQuery().getQueryMap().forEach((k, v) -> params.put(k.toString(), v));
            req.put("params", params);

            ctx.put("req", req);
        }
        return ctx;
    }

    public static Map<String, Object> buildContext(Response response) {
        if (response != null) {
            Map<String, Object> ctx = buildContext(response.getRequest());
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", response.getStatus());
            resp.put("headers", response.getHeaders());
            ctx.put("resp", resp);
            return ctx;
        }
        return buildContext();
    }
}
