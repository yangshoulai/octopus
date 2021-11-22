package com.octopus.core;

import cn.hutool.core.net.url.UrlBuilder;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
public class Request implements Serializable, Comparable<Request> {

  private String id;

  private String url;

  private RequestMethod method;

  private byte[] body;

  private Map<String, String> params = new HashMap<>();

  private Map<String, String> headers = new HashMap<>();

  private int priority;

  private boolean repeatable = true;

  private Map<String, Object> attributes = new HashMap<>();

  private String parent;

  public Request() {}

  public Request(@NonNull String url, @NonNull RequestMethod method) {
    this.url = url;
    this.method = method;
  }

  public String getUrl() {
    return url;
  }

  public Request setUrl(@NonNull String url) {
    this.url = url;
    return this;
  }

  public RequestMethod getMethod() {
    return method;
  }

  public Request setMethod(@NonNull RequestMethod method) {
    this.method = method;
    return this;
  }

  public String getId() {
    return id;
  }

  public Request setId(@NonNull String id) {
    this.id = id;
    return this;
  }

  public byte[] getBody() {
    return body;
  }

  public Request setBody(byte[] body) {
    this.body = body;
    return this;
  }

  public Request setHeaders(@NonNull Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public Request setParams(Map<String, String> params) {
    this.params = params;
    return this;
  }

  public int getPriority() {
    return priority;
  }

  public Request setPriority(int priority) {
    this.priority = priority;
    return this;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public Request setRepeatable(boolean repeatable) {
    this.repeatable = repeatable;
    return this;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(@NonNull Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public Request putAttribute(String attr, Object value) {
    this.attributes.put(attr, value);
    return this;
  }

  public Request putAttributes(Map<String, Object> attrs) {
    this.attributes.putAll(attrs);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String attr) {
    return (T) this.attributes.get(attr);
  }

  public <T> T getAttribute(String attr, T defaultValue) {
    T value = this.getAttribute(attr);
    return value == null ? defaultValue : value;
  }

  public Request setParent(@NonNull String parent) {
    this.parent = parent;
    return this;
  }

  public String getParent() {
    return parent;
  }

  @Override
  public int compareTo(Request o) {
    return o.getPriority() - this.getPriority();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Request request = (Request) o;
    return this.getId() != null && this.getId().equals(request.getId());
  }

  @Override
  public int hashCode() {
    return this.getId() == null ? 0 : this.getId().hashCode();
  }

  @Override
  public String toString() {
    UrlBuilder builder = UrlBuilder.ofHttpWithoutEncode(url);
    if (this.params != null) {
      params.forEach(builder::addQuery);
    }
    return String.format("%s", builder.build());
  }

  public static Request get(String url) {
    return new Request(url, RequestMethod.GET);
  }

  public static Request post(String url) {
    return new Request(url, RequestMethod.POST);
  }

  public enum RequestMethod {
    /** GET */
    GET("GET"),
    /** POST */
    POST("POST");

    private final String name;

    RequestMethod(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}
