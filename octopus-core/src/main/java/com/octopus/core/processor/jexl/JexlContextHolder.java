package com.octopus.core.processor.jexl;

import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/07/11
 */
public class JexlContextHolder {

    public static final String KEY_REQUEST = "$request";

    public static final String KEY_RESPONSE = "$response";

    public static final String KEY_RESULT = "$result";

    public static final String KEY_LINK = "$link";

    public static final String KEY_SELECTED = "$selected";

    private static final ThreadLocal<Map<String, Object>> CONTEXT = new InheritableThreadLocal<>();

    public static void setContext(Map<String, Object> context) {
        CONTEXT.set(context);
    }

    public static Map<String, Object> getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
