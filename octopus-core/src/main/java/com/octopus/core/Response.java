package com.octopus.core;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
@Getter
public class Response implements Serializable {

    /**
     * 请求
     */
    private Request request;

    /**
     * 响应状态
     */
    private int status;

    /**
     * 响应体
     */
    private byte[] body = new byte[0];

    /**
     * 响应头
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 响应编码
     */
    private String charset;

    /**
     * 媒体类型
     */
    private String mimeType;


    public Response() {
    }

    public Response(@NonNull Request request) {
        this.request = request;
    }

    public Response setRequest(Request request) {
        this.request = request;
        return this;
    }


    public Response setStatus(int status) {
        this.status = status;
        return this;
    }


    public Response setBody(byte[] body) {
        this.body = body;
        return this;
    }


    public Response setHeaders(@NonNull Map<String, String> headers) {
        this.headers = headers;
        return this;
    }


    public Response setCharset(String charset) {
        this.charset = charset;
        return this;
    }


    public Response setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }


    public final boolean isSuccessful() {
        return this.status >= 200 && this.status < 300;
    }

    public String asText() {
        return new String(this.body, Charset.forName(this.charset));
    }

    public Document asDocument() {
        return Jsoup.parse(this.asText());
    }

    public JSON asJson() {
        return JSONUtil.parse(this.asText());
    }

    @Override
    public String toString() {
        return String.format(
                "Response[status = %s, request = %s, charset = %s, length = %s bytes]",
                status, request, charset, body.length);
    }
}
