package com.octopus.core.processor.extractor.configurable;

import cn.hutool.core.collection.ListUtil;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 字段类型
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public enum FieldType {
    /**
     * BigDecimal
     */
    BigDecimal(ListUtil.of(BigDecimal.class)),

    /**
     * String
     */
    String(ListUtil.of(String.class)),

    /**
     * Integer
     */
    Integer(ListUtil.of(Integer.class, int.class)),

    /**
     * Double
     */
    Double(ListUtil.of(Double.class, double.class)),

    /**
     * Boolean
     */
    Boolean(ListUtil.of(Boolean.class, boolean.class)),

    /**
     * Character
     */
    Character(ListUtil.of(Character.class, char.class)),

    /**
     * CharSequence
     */
    CharSequence(ListUtil.of(CharSequence.class, String.class)),

    /**
     * Float
     */
    Float(ListUtil.of(Float.class, float.class)),
    /**
     * Long
     */
    Long(ListUtil.of(java.lang.Long.class, long.class)),
    /**
     * Date
     */
    Date(ListUtil.of(java.util.Date.class));

    /**
     * 支持的 Java 类型
     */
    private final List<Class<?>> supportedClasses;

    FieldType(List<Class<?>> supportedClasses) {
        this.supportedClasses = supportedClasses;
    }

    /**
     * 是否支持该类型
     *
     * @param cls 类型
     * @return 是否支持
     */
    public boolean isSupport(Class<?> cls) {
        return supportedClasses.contains(cls);
    }
}
