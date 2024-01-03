package com.octopus.core.processor.extractor.type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class TypeHandlerRegistry {

    private static final Map<Class<?>, Class<?>> COLLECTION_CLASS_MAPPING = new HashMap<>();

    private static final Map<Class<?>, TypeHandler<?>> HANDLERS = new HashMap<>();

    private TypeHandlerRegistry() {
        registerCollection(Collection.class, ArrayList.class);
        registerCollection(List.class, ArrayList.class);
        registerCollection(ArrayList.class, ArrayList.class);
        registerCollection(Set.class, HashSet.class);
        registerCollection(HashSet.class, HashSet.class);
        registerCollection(LinkedList.class, LinkedList.class);

        registerHandler(int.class, new IntegerTypeHandler());
        registerHandler(Integer.class, new IntegerTypeHandler());
        registerHandler(long.class, new LongTypeHandler());
        registerHandler(Long.class, new LongTypeHandler());
        registerHandler(boolean.class, new BooleanTypeHandler());
        registerHandler(Boolean.class, new BooleanTypeHandler());
        registerHandler(double.class, new DoubleTypeHandler());
        registerHandler(Double.class, new DoubleTypeHandler());
        registerHandler(float.class, new FloatTypeHandler());
        registerHandler(Float.class, new FloatTypeHandler());
        registerHandler(BigDecimal.class, new BigDecimalTypeHandler());
        registerHandler(char.class, new CharacterTypeHandler());
        registerHandler(Character.class, new CharacterTypeHandler());
        registerHandler(String.class, new CharSequenceTypeHandler());
        registerHandler(CharSequence.class, new CharSequenceTypeHandler());
        registerHandler(Date.class, new DateTypeHandler());
        registerHandler(Object.class, new CharSequenceTypeHandler());
    }

    public static TypeHandlerRegistry getInstance() {
        return Holder.INSTANCE;
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

    public TypeHandler<?> getTypeHandler(Class<?> clz) {
        return HANDLERS.get(clz);
    }

    public void registerHandler(@NonNull Class<?> typeClass, @NonNull TypeHandler<?> handler) {
        HANDLERS.put(typeClass, handler);
    }

    private static class Holder {
        public static final TypeHandlerRegistry INSTANCE = new TypeHandlerRegistry();
    }
}
