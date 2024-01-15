package com.octopus.core.processor.extractor.convert;

import com.octopus.core.configurable.FieldExtProperties;
import com.octopus.core.configurable.FieldType;
import com.octopus.core.processor.extractor.annotation.FieldExt;
import lombok.NonNull;

import java.util.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class TypeConverterRegistry {
    private static final Map<Class<?>, Class<?>> COLLECTION_CLASS_MAPPING = new HashMap<>();
    private static final Map<FieldType, TypeConverter<?>> HANDLERS = new HashMap<>();

    private TypeConverterRegistry() {
        registerCollection(Collection.class, ArrayList.class);
        registerCollection(List.class, ArrayList.class);
        registerCollection(ArrayList.class, ArrayList.class);
        registerCollection(Set.class, HashSet.class);
        registerCollection(HashSet.class, HashSet.class);
        registerCollection(LinkedList.class, LinkedList.class);

        registerHandler(FieldType.BigDecimal, new BigDecimalTypeConverter());
        registerHandler(FieldType.Integer, new IntegerTypeConverter());
        registerHandler(FieldType.Long, new LongTypeConverter());
        registerHandler(FieldType.Boolean, new BooleanTypeConverter());
        registerHandler(FieldType.Double, new DoubleTypeConverter());
        registerHandler(FieldType.Float, new FloatTypeConverter());
        registerHandler(FieldType.BigDecimal, new BooleanTypeConverter());
        registerHandler(FieldType.Character, new CharacterTypeConverter());
        registerHandler(FieldType.String, new CharSequenceTypeConverter());
        registerHandler(FieldType.CharSequence, new CharSequenceTypeConverter());
        registerHandler(FieldType.Date, new DateTypeConverter());
        registerHandler(FieldType.ByteArray, new ByteArrayTypeConverter());
    }

    public static TypeConverterRegistry getInstance() {
        return Holder.INSTANCE;
    }


    public TypeConverter<?> getTypeHandler(FieldType type) {
        return HANDLERS.get(type);
    }

    public FieldType getFieldType(Class<?> type) {
        for (Map.Entry<FieldType, TypeConverter<?>> entry : HANDLERS.entrySet()) {
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
        for (Map.Entry<FieldType, TypeConverter<?>> entry : HANDLERS.entrySet()) {
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


    public void registerHandler(@NonNull FieldType type, @NonNull TypeConverter<?> handler) {
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
        public static final TypeConverterRegistry INSTANCE = new TypeConverterRegistry();
    }
}
