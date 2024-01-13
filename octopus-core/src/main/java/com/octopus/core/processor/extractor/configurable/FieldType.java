package com.octopus.core.processor.extractor.configurable;

import cn.hutool.core.collection.ListUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public enum FieldType {
    BigDecimal(ListUtil.of(BigDecimal.class)),

    String(ListUtil.of(String.class)),

    Integer(ListUtil.of(Integer.class, int.class)),

    Double(ListUtil.of(Double.class, double.class)),

    Boolean(ListUtil.of(Boolean.class, boolean.class)),

    Character(ListUtil.of(Character.class, char.class)),

    CharSequence(ListUtil.of(CharSequence.class, String.class)),

    Float(ListUtil.of(Float.class, float.class)),

    Long(ListUtil.of(java.lang.Long.class, long.class)),

    Date(ListUtil.of(java.util.Date.class));

    private List<Class<?>> supportedClasses;

    FieldType(List<Class<?>> supportedClasses) {
        this.supportedClasses = supportedClasses;
    }

    public List<Class<?>> getSupportedClasses() {
        return supportedClasses;
    }

    public boolean isSupport(Class<?> cls) {
        return supportedClasses.contains(cls);
    }
}
