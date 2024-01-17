package com.octopus.core.processor;

import com.octopus.core.properties.FieldExtProperties;
import com.octopus.core.properties.FieldType;
import com.octopus.core.processor.annotation.FieldExt;
import com.octopus.core.processor.converter.*;
import lombok.NonNull;

import java.util.*;

/**
 * 类型转换注册器
 *
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class ConverterRegistry {
    private static final Map<Class<?>, Class<?>> COLLECTION_CLASS_MAPPING = new HashMap<>();
    private static final Map<FieldType, Converter<?>> HANDLERS = new HashMap<>();

    private ConverterRegistry() {
        registerCollection(Collection.class, ArrayList.class);
        registerCollection(List.class, ArrayList.class);
        registerCollection(ArrayList.class, ArrayList.class);
        registerCollection(Set.class, HashSet.class);
        registerCollection(HashSet.class, HashSet.class);
        registerCollection(LinkedList.class, LinkedList.class);

        registerHandler(FieldType.BigDecimal, new BigDecimalConverter());
        registerHandler(FieldType.Integer, new IntegerConverter());
        registerHandler(FieldType.Long, new LongConverter());
        registerHandler(FieldType.Boolean, new BooleanConverter());
        registerHandler(FieldType.Double, new DoubleConverter());
        registerHandler(FieldType.Float, new FloatConverter());
        registerHandler(FieldType.BigDecimal, new BooleanConverter());
        registerHandler(FieldType.Character, new CharacterConverter());
        registerHandler(FieldType.String, new CharSequenceConverter());
        registerHandler(FieldType.CharSequence, new CharSequenceConverter());
        registerHandler(FieldType.Date, new DateConverter());
        registerHandler(FieldType.ByteArray, new ByteArrayConverter());
    }

    public static ConverterRegistry getInstance() {
        return Holder.INSTANCE;
    }


    public Converter<?> getTypeHandler(FieldType type) {
        return HANDLERS.get(type);
    }

    public FieldType getFieldType(Class<?> type) {
        for (Map.Entry<FieldType, Converter<?>> entry : HANDLERS.entrySet()) {
            if (entry.getKey().isSupport(type)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isSupportType(@NonNull Class<?> type) {

        return getFieldType(type) != null;
    }

    public Object convert(String source, Class<?> type, FieldExt ext) {
        for (Map.Entry<FieldType, Converter<?>> entry : HANDLERS.entrySet()) {
            if (entry.getKey().isSupport(type)) {
                FieldExtProperties fieldExtProperties = new FieldExtProperties();
                if (ext != null) {
                    fieldExtProperties.setIgnoreError(ext.ignoreError());
                    fieldExtProperties.setDateFormatPattern(ext.dateFormatPattern());
                    fieldExtProperties.setDateFormatTimeZone(ext.dateFormatTimeZone());
                    fieldExtProperties.setBooleanFalseValues(ext.booleanFalseValues());
                }
                return entry.getValue().convert(source, fieldExtProperties);
            }
        }
        return null;
    }


    public void registerHandler(@NonNull FieldType type, @NonNull Converter<?> handler) {
        HANDLERS.put(type, handler);
    }

    public boolean isValidCollectionType(@NonNull Class<?> cls) {
        return COLLECTION_CLASS_MAPPING.containsKey(cls);
    }

    public Class<?> getCollectionImplClass(@NonNull Class<?> cls) {
        return COLLECTION_CLASS_MAPPING.get(cls);
    }

    public void registerCollection(@NonNull Class<?> interfaceClass, @NonNull Class<?> implClass) {
        if (!Collection.class.isAssignableFrom(interfaceClass)) {
            throw new IllegalArgumentException("Class " + interfaceClass + " is not a collection");
        }
        if (!Collection.class.isAssignableFrom(implClass)) {
            throw new IllegalArgumentException("Class " + implClass + " is not a collection");
        }
        if (implClass.isInterface()) {
            throw new IllegalArgumentException("Class " + implClass + " can not be an interface");
        }

        if (!interfaceClass.equals(implClass) && !interfaceClass.isAssignableFrom(implClass)) {
            throw new IllegalArgumentException(
                    "Impl class "
                            + implClass
                            + " must be or implements from interface class "
                            + interfaceClass);
        }
        try {
            implClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Class " + implClass + " must provide a default constructor");
        }
        COLLECTION_CLASS_MAPPING.put(interfaceClass, implClass);
    }

    private static class Holder {
        public static final ConverterRegistry INSTANCE = new ConverterRegistry();
    }
}
