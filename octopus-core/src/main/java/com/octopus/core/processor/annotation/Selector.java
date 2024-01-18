package com.octopus.core.processor.annotation;

import java.lang.annotation.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/27
 */
@Documented
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Selector {

    /**
     * 类型
     *
     * @return 选择器类型
     */
    Type type() default Type.Css;

    /**
     * 选择器表达式
     *
     * @return 选择器表达式
     */
    String value() default "";

    /**
     * 默认值
     *
     * @return 默认值
     */
    String def() default "";

    /**
     * @return CSS选择器 元素属性名称
     */
    String attr() default "";

    /**
     * @return CSS选择器 是否选中元素自身
     */
    boolean self() default false;

    /**
     * @return 正则选择器 匹配组
     */
    int[] groups() default {0};

    /**
     * @return 正则选择器 匹配后格式化表达式
     */
    String format() default "%s";

    /**
     * @return XPATH选择器 是否节点
     */
    boolean node() default true;

    /**
     * @return 格式化
     */
    Denoiser denoiser() default @Denoiser();

    enum Type {
        /**
         * Body
         */
        Body,
        /**
         * Attr
         */
        Attr,
        /**
         * Css
         */
        Css,
        /**
         * Json
         */
        Json,
        /**
         * Param
         */
        Param,
        /**
         * Header
         */
        Header,
        /**
         * Regex
         */
        Regex,
        /**
         * Url
         */
        Url,
        /**
         * Xpath
         */
        Xpath,

        /**
         * Value
         */
        Value,
        /**
         * None
         */
        None
    }
}
