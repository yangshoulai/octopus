package com.octopus.core.utils;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpUtil;
import com.octopus.core.Request;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class RequestHelper {

    public static String generateId(Request request) {
        String url = request.getUrl();
        UrlBuilder urlBuilder = UrlBuilder.of(url);
        if (request.getParams() != null) {
            request.getParams().forEach(urlBuilder::addQuery);
        }
        Map<CharSequence, CharSequence> map = urlBuilder.getQuery().getQueryMap();
        UrlQuery query = new UrlQuery(new TreeMap<>(map));
        urlBuilder.setQuery(query);
        url = urlBuilder.build();
        byte[] requestBytes =
                ArrayUtil.addAll(
                        (request.getMethod() + url).getBytes(StandardCharsets.UTF_8), request.getBody());
        return MD5.create().digestHex(requestBytes);
    }

    public static String completeUrl(String parentUrl, String targetUrl) {
        if (!HttpUtil.isHttp(targetUrl) && !HttpUtil.isHttps(targetUrl)) {
            if (targetUrl.startsWith("/")) {
                return URLUtil.completeUrl(parentUrl, targetUrl);
            } else {
                targetUrl = UrlBuilder.of(parentUrl).setQuery(UrlQuery.of(targetUrl, CharsetUtil.CHARSET_UTF_8)).build();
            }
        }
        return targetUrl;
    }
}
