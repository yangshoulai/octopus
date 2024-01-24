package com.octopus.core.processor.jexl.func;

import com.octopus.core.processor.jexl.JexlFunc;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/24
 */
public class EnvJexlFunc implements JexlFunc {

    public String get(String name) {
        String v = System.getProperty(name);
        if (v != null) {
            return v;
        }
        return System.getenv(name);
    }

    @Override
    public String getFuncName() {
        return "env";
    }
}
