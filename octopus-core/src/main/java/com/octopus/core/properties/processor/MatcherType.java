package com.octopus.core.properties.processor;

/**
 * 匹配器类型
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public enum MatcherType {

    /**
     * 基于 URL 的正则匹配器
     */
    UrlRegex,

    /**
     * 基于响应头的正则匹配器
     */
    HeaderRegex,

    /**
     * 基于响应内容格式的正则匹配器
     */
    ContentTypeRegex,

    /**
     * 匹配所有请求
     */
    All,

    /**
     * JSON 类型匹配器
     */
    Json,

    /**
     * Html 类型匹配器
     */
    Html,

    /**
     * Image 类型匹配器
     */
    Image,

    /**
     * Video 类型匹配器
     */
    Video,

    /**
     * Pdf 类型匹配器
     */
    Pdf,

    /**
     * Word 类型匹配器
     */
    Word,

    /**
     * Excel 类型匹配器
     */
    Excel,

    /**
     * Audio 类型匹配器
     */
    Audio,

    /**
     * 流类型匹配器
     */
    OctetStream,

    /**
     * 媒体类型匹配器
     */
    Media,

    /**
     * 且组合匹配器
     */
    And,

    /**
     * 或组合匹配器
     */
    Or,
    /**
     * 非匹配器
     */
    Not
}
