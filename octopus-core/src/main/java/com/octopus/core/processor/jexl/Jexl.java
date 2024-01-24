package com.octopus.core.processor.jexl;

import com.octopus.core.processor.jexl.func.DecodeJexlFunc;
import com.octopus.core.processor.jexl.func.EncodeJexlFunc;
import com.octopus.core.processor.jexl.func.EnvJexlFunc;
import com.octopus.core.processor.jexl.func.RegexJexlFunc;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/24
 */
public class Jexl {

    private static JexlEngine ENGINE = null;

    static {
        DecodeJexlFunc decode = new DecodeJexlFunc();
        EncodeJexlFunc encode = new EncodeJexlFunc();
        RegexJexlFunc regex = new RegexJexlFunc();
        EnvJexlFunc env = new EnvJexlFunc();
        Map<String, Object> ns = new HashMap<>();
        ns.put(decode.getFuncName(), decode);
        ns.put(encode.getFuncName(), encode);
        ns.put(regex.getFuncName(), regex);
        ns.put(env.getFuncName(), env);
        ENGINE = new JexlBuilder().cache(512).strict(true).silent(false).namespaces(ns).create();
    }

    public static Object eval(String expression, Map<String, Object> context) {
        MapContext ctx = new MapContext(context);
        try {
            return ENGINE.createExpression(expression).evaluate(ctx);
        } catch (Exception ignore) {
        }
        try {
            return ENGINE.createScript(expression).execute(ctx);
        } catch (Exception ignore) {

        }
        return null;
    }

}
