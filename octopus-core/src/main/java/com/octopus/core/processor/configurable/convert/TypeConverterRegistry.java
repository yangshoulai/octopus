package com.octopus.core.processor.configurable.convert;

import com.octopus.core.processor.configurable.FieldType;
import lombok.NonNull;

import java.util.*;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class TypeConverterRegistry {

    private static final Map<FieldType, TypeConverter<?>> HANDLERS = new HashMap<>();

    private TypeConverterRegistry() {
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
    }

    public static TypeConverterRegistry getInstance() {
        return Holder.INSTANCE;
    }


    public TypeConverter<?> getTypeHandler(FieldType type) {
        return HANDLERS.get(type);
    }

    public void registerHandler(@NonNull FieldType type, @NonNull TypeConverter<?> handler) {
        HANDLERS.put(type, handler);
    }

    private static class Holder {
        public static final TypeConverterRegistry INSTANCE = new TypeConverterRegistry();
    }
}
