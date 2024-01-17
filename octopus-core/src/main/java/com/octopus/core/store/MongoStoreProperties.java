package com.octopus.core.store;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import lombok.Data;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/17
 */
@Data
public class MongoStoreProperties implements Validatable, Transformable<MongoStore> {

    public static final String DEFAULT_COLLECTION = "request";

    public static final String DEFAULT_DATABASE = "octopus";

    public static final String DEFAULT_URI = "mongodb://127.0.0.1:27017/";
    /**
     * Mongo 存储器 主机
     * <p>
     * 默认 mongodb://127.0.0.1:27017/
     */
    private String uri = DEFAULT_URI;

    /**
     * Mongo 存储器 数据库名称
     * <p>
     * 默认 octopus
     */
    private String database = DEFAULT_DATABASE;

    /**
     * Mongo 存储器 数据库集合名称
     * <p>
     * 默认 request
     */
    private String collection = DEFAULT_COLLECTION;

    public MongoStoreProperties() {
    }

    public MongoStoreProperties(@NonNull String uri) {
        this.uri = uri;
    }

    public MongoStoreProperties(@NonNull String uri, @NonNull String database) {
        this.uri = uri;
        this.database = database;
    }

    public MongoStoreProperties(@NonNull String uri, @NonNull String database, @NonNull String collection) {
        this.uri = uri;
        this.database = database;
        this.collection = collection;
    }

    @Override
    public MongoStore transform() {
        return new MongoStore(this);
    }

    @Override
    public void validate() throws ValidateException {
        if (StrUtil.isBlank(uri)) {
            throw new ValidateException("mongo uri is required");
        }
        if (StrUtil.isBlank(database)) {
            throw new ValidateException("mongo database is required");
        }

        if (StrUtil.isBlank(collection)) {
            throw new ValidateException("mongo collection is required");
        }
    }
}
