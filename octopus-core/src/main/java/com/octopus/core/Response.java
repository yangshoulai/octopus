package com.octopus.core;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public class Response implements Serializable {

  private Request request;

  private int status;

  private byte[] body = new byte[0];

  private Map<String, String> headers = new HashMap<>();

  private Charset charset;

  private String mimeType;

  public Response(@NonNull Request request) {
    this.request = request;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public Request getRequest() {
    return request;
  }

  public int getStatus() {
    return status;
  }

  public Response setStatus(int status) {
    this.status = status;
    return this;
  }

  public byte[] getBody() {
    return body;
  }

  public Response setBody(byte[] body) {
    this.body = body;
    return this;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public Response setHeaders(@NonNull Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public Charset getCharset() {
    return charset;
  }

  public Response setCharset(Charset charset) {
    this.charset = charset;
    return this;
  }

  public String getMimeType() {
    return mimeType;
  }

  public Response setMimeType(String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  public String asText() {
    return new String(this.body, this.charset);
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
        "Response [status = %s, request = %s, charset = %s, length = %s bytes]",
        status, request, charset, body.length);
  }
}
