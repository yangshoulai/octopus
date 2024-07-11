package com.octopus.core;

import cn.hutool.core.net.url.UrlBuilder;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/19
 */
@Getter
public class Request implements Serializable, Comparable<Request> {

    /**
     * 请求唯一编号（自动生成）
     * <p>
     * 由请求方法、请求链接、查询参数、查询体生成
     */
    private String id;

    /**
     * 请求链接
     */
    private String url;

    /**
     * 请求方法
     */
    private RequestMethod method;

    /**
     * 请求体
     */
    private byte[] body;

    /**
     * 请求查询参数
     */
    private Map<String, String> params = new HashMap<>();

    /**
     * 请求头
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 请求优先级
     */
    private int priority;

    /**
     * 是否可重复
     * <p>
     * ID一致的请求被认为是重复请求
     */
    private boolean repeatable = true;

    /**
     * 请求属性
     * <p>
     * 自定义的请求属性，方便在父子请求之间传递业务属性
     */
    private Map<String, Object> attrs = new HashMap<>();

    /**
     * 父请求 ID
     */
    private String parent;

    /**
     * 请求的索引
     * <p>
     * 记录子请求在父请求页面出现的位置
     */
    private int index;

    /**
     * 创建时间
     */
    private Date createDate = new Date();

    /**
     * 是否缓存
     * <p>
     * 避免重新下载，当前只有 RedisStore 支持
     */
    private boolean cache = false;

    /**
     * 是否从父请求集成属性
     */
    private boolean inherit = false;

    /**
     * 请求状态
     */
    private Status status = Status.of(State.Waiting);

    /**
     * 失败次数
     */
    private int failTimes = 0;

    /**
     * 请求深度
     * <p>
     * 从种子开始逐层递增
     */
    private int depth = 0;

    public Request() {
    }

    public Request(@NonNull String url) {
        this(url, RequestMethod.GET);
    }

    public Request(@NonNull String url, @NonNull RequestMethod method) {
        this.setUrl(url);
        this.method = method;
    }

    public static Request get(String url) {
        return new Request(url, RequestMethod.GET);
    }

    public static Request post(String url) {
        return new Request(url, RequestMethod.POST);
    }

    public Request setUrl(@NonNull String url) {
        this.url = url;
        return this;
    }

    public Request setMethod(@NonNull RequestMethod method) {
        this.method = method;
        return this;
    }

    public Request setId(@NonNull String id) {
        this.id = id;
        return this;
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

    public Request setHeaders(@NonNull Map<String, String> headers) {
        this.headers = headers;
        return this;
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

    public Request setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public Request setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
        return this;
    }

    public void setAttrs(@NonNull Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    public Request putAttribute(@NonNull String attr, Object value) {
        this.attrs.put(attr, value);
        return this;
    }

    public Request putAttributes(@NonNull Map<String, Object> attrs) {
        this.attrs.putAll(attrs);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(@NonNull String attr) {
        return (T) this.attrs.get(attr);
    }

    public <T> T getAttribute(String attr, T defaultValue) {
        T value = this.getAttribute(attr);
        return value == null ? defaultValue : value;
    }

    public Request setParent(@NonNull String parent) {
        this.parent = parent;
        return this;
    }

    public Request setInherit(boolean inherit) {
        this.inherit = inherit;
        return this;
    }

    public Request setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Request setFailTimes(int failTimes) {
        this.failTimes = failTimes;
        return this;
    }

    public Request setDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public Request setCache(boolean cache) {
        this.cache = cache;
        return this;
    }

    public Request setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Request setIndex(int index) {
        this.index = index;
        return this;
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

    @Getter
    public enum RequestMethod {
        /**
         * GET
         */
        GET("GET"),
        /**
         * POST
         */
        POST("POST");

        private final String name;

        RequestMethod(String name) {
            this.name = name;
        }

    }

    public enum State {
        /**
         * 等待处理
         */
        Waiting,
        /**
         * 正在处理
         */
        Executing,
        /**
         * 失败
         */
        Failed,
        /**
         * 完成
         */
        Completed
    }

    @Getter
    public static class Status {

        private State state;

        private String message;

        public Status() {
        }

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

    }
}
