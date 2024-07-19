package com.octopus.core.properties.store;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.octopus.core.OctopusBuilder;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.logging.Logger;
import com.octopus.core.logging.LoggerFactory;
import com.octopus.core.store.*;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * 请求存储器配置
 * <p>
 * 默认使用内存
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class StoreProperties implements Validatable, Transformable<Store> {
    private static final Logger logger = LoggerFactory.getLogger(OctopusBuilder.class);
    /**
     * Redis Store
     */
    private RedisStoreProperties redis;

    /**
     * Mongo Store
     */
    private MongoStoreProperties mongo;

    /**
     * Sqlite Store
     */
    private SqliteStoreProperties sqlite;

    /**
     * Custom store
     */
    private CustomStoreProperties custom;

    public StoreProperties() {
    }

    @Override
    public void validate() throws ValidateException {
        Validator.validateWhenNotNull(redis);
        Validator.validateWhenNotNull(mongo);
        Validator.validateWhenNotNull(sqlite);
    }

    @Override
    public Store transform() {
        Store store = null;
        if (this.custom != null) {
            Class<? extends AbstractCustomStore> cls = ClassUtil.loadClass(custom.getStore());
            try {
                store = ReflectUtil.getConstructor(cls, Properties.class).newInstance(custom.getConf());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (this.redis != null) {
            store = this.redis.transform();
        } else if (this.mongo != null) {
            store = this.mongo.transform();
        } else if (this.sqlite != null) {
            store = this.sqlite.transform();
        } else {
            store = new MemoryStore();
        }
        logger.debug("Using store => " + store.getClass().getSimpleName());
        return store;
    }
}
