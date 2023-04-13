package com.octopus.core;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.URLUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
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

  /** 是否从父请求集成属性 */
  private boolean inherit = false;

  private Status status = Status.of(State.Waiting);

  /** 失败次数 */
  private int failTimes = 0;

  public Request() {}

  public Request(@NonNull String url, @NonNull RequestMethod method) {
    this.url = URLUtil.normalize(url);
    this.method = method;
  }

  public static Request get(String url) {
    return new Request(url, RequestMethod.GET);
  }

  public static Request post(String url) {
    return new Request(url, RequestMethod.POST);
  }

  public String getUrl() {
    return url;
  }

  public Request setUrl(@NonNull String url) {
    this.url = URLUtil.normalize(url);
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

  public Request addHeaders(@NonNull Map<String, String> headers) {
    this.headers.putAll(headers);
    return this;
  }

  public Request addHeader(@NonNull String header, String value) {
    this.headers.put(header, value);
    return this;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public Request setHeaders(@NonNull Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public Request setParams(@NonNull Map<String, String> params) {
    this.params = params;
    return this;
  }

  public Request addParam(@NonNull String param, String value) {
    this.params.put(param, value);
    return this;
  }

  public Request addParams(@NonNull Map<String, String> params) {
    this.params.putAll(params);
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

  public Request putAttribute(@NonNull String attr, Object value) {
    this.attributes.put(attr, value);
    return this;
  }

  public Request putAttributes(@NonNull Map<String, Object> attrs) {
    this.attributes.putAll(attrs);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttribute(@NonNull String attr) {
    return (T) this.attributes.get(attr);
  }

  public <T> T getAttribute(String attr, T defaultValue) {
    T value = this.getAttribute(attr);
    return value == null ? defaultValue : value;
  }

  public String getParent() {
    return parent;
  }

  public Request setParent(@NonNull String parent) {
    this.parent = parent;
    return this;
  }

  public boolean isInherit() {
    return inherit;
  }

  public Request setInherit(boolean inherit) {
    this.inherit = inherit;
    return this;
  }

  public Status getStatus() {
    return status;
  }

  public Request setStatus(Status status) {
    this.status = status;
    return this;
  }

  public int getFailTimes() {
    return failTimes;
  }

  public void setFailTimes(int failTimes) {
    this.failTimes = failTimes;
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

  public enum State {
    /** 等待处理 */
    Waiting,
    /** 正在处理 */
    Executing,
    /** 失败 */
    Failed,
    /** 完成 */
    Completed
  }

  @Data
  public static class Status {

    private State state;

    private String message;

    public Status() {}

    public Status(@NonNull State state, String message) {
      this.state = state;
      this.message = message;
    }

    public Status(@NonNull State state) {
      this.state = state;
    }

    public static Status of(@NonNull State state, String message) {
      return new Status(state, message);
    }

    public static Status of(@NonNull State state) {
      return new Status(state);
    }

    public State getState() {
      return state;
    }

    public String getMessage() {
      return message;
    }
  }
}
