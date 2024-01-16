package com.octopus.core.downloader;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.ssl.TrustAnyHostnameVerifier;
import com.octopus.core.Request;
import com.octopus.core.Request.RequestMethod;
import com.octopus.core.Response;
import com.octopus.core.exception.DownloadException;

import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class OkHttpDownloader extends AbstractDownloader {
    private final ConnectionPool connectionPool;

    public OkHttpDownloader() {
        this.connectionPool = new ConnectionPool();
    }

    @Override
    public Response download(Request request, DownloadConfig config) throws DownloadException {
        Proxy proxy = this.resolveProxy(config.getProxyProvider(), request);
        try (okhttp3.Response response =
                     new OkHttpClient.Builder()
                             .connectionPool(this.connectionPool)
                             .hostnameVerifier(new TrustAnyHostnameVerifier())
                             .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                             .readTimeout(config.getSocketTimeout(), TimeUnit.MILLISECONDS)
                             .writeTimeout(config.getSocketTimeout(), TimeUnit.MILLISECONDS)
                             .callTimeout(
                                     2L * (config.getConnectTimeout() + config.getSocketTimeout()),
                                     TimeUnit.MILLISECONDS)
                             .proxy(proxy)
                             .build()
                             .newCall(createRequest(request, config))
                             .execute()) {
            Response r = new Response(request);
            r.setStatus(response.code());
            Charset charset = null;
            String mineType = null;
            ResponseBody body = response.body();
            if (body != null) {
                r.setBody(body.bytes());
                MediaType mediaType = body.contentType();
                if (mediaType != null) {
                    charset = mediaType.charset() == null ? config.getCharset() : mediaType.charset();
                    mineType = mediaType.type();
                }
            }
            r.setCharset(charset == null ? config.getCharset() : charset);
            r.setMimeType(mineType);
            response
                    .headers()
                    .forEach(
                            p -> {
                                r.getHeaders().put(p.getFirst(), p.getSecond());
                            });
            return r;
        } catch (IOException e) {
            throw new DownloadException(
                    String.format(
                            "Fetch [%s] via proxy [%s] failed, caused by %s", request, proxy, e.getMessage()),
                    e);
        }
    }

    private okhttp3.Request createRequest(Request request, DownloadConfig config) {
        UrlBuilder urlBuilder = UrlBuilder.ofHttpWithoutEncode(request.getUrl());
        if (request.getParams() != null) {
            request.getParams().forEach(urlBuilder::addQuery);
        }
        String url = urlBuilder.build();
        Map<String, String> allHeaders =
                MapUtil.builder("Host", URLUtil.url(request.getUrl()).getHost())
                        .putAll(config.getHeaders())
                        .putAll(request.getHeaders())
                        .build();
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
        allHeaders.forEach(builder::addHeader);
        if (request.getMethod() == RequestMethod.POST) {
            builder.post(RequestBody.create(request.getBody() == null ? new byte[0] : request.getBody()));
        } else {
            builder.get();
        }
        return builder.build();
    }
}
