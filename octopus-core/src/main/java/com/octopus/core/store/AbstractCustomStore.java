package com.octopus.core.store;

import java.util.Properties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/19
 */
public abstract class AbstractCustomStore implements Store {

    protected final Properties conf;
    public AbstractCustomStore(Properties conf) {
        this.conf = conf;
    }
}
