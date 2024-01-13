package com.octopus.core.configuration;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.store.*;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class StoreProperties implements Validator {

    private StoreType type = StoreType.Memory;

    // redis
    private String redisHost = "127.0.0.1";

    private int redisPort = 6379;

    private String redisKeyPrefix = "octopus";

    // sqlite
    private String sqliteDatabaseFilePath;

    private String sqliteTableName = "requests";


    // mongo
    private String mongoHost = "127.0.0.1";

    private int mongoPort = 27017;

    private String mongoDatabase = "octopus";

    private String mongoCollection = "request";

    public Store toStore() {
        switch (type) {
            case Redis:
                return new RedisStore(redisKeyPrefix, redisHost, redisPort);
            case SQLite:
                return new SQLiteStore(sqliteDatabaseFilePath, sqliteTableName);
            case Memory:
                return new MemoryStore();
            case Mongo:
                return new MongoStore(mongoDatabase, mongoCollection, mongoHost, mongoPort);
            default:
                return null;
        }
    }

    @Override
    public void validate() throws ValidateException {
        if (type == null) {
            throw new ValidateException("store type is required");
        }

        if (type == StoreType.Redis) {
            if (StrUtil.isBlank(redisHost)) {
                throw new ValidateException("redis host is required");
            }
            if (redisPort <= 0) {
                throw new ValidateException("redis port is invalid");
            }
            if (StrUtil.isBlank(redisKeyPrefix)) {
                throw new ValidateException("redis key prefix is required");
            }
        }

        if (type == StoreType.SQLite) {
            if (StrUtil.isBlank(sqliteDatabaseFilePath)) {
                throw new ValidateException("sqlite database file path is required");
            }
            if (StrUtil.isBlank(sqliteTableName)) {
                throw new ValidateException("sqlite database table name is required");
            }
        }

        if (type == StoreType.Mongo) {
            if (StrUtil.isBlank(mongoHost)) {
                throw new ValidateException("mongo host is required");
            }
            if (mongoPort <= 0) {
                throw new ValidateException("mongo port is invalid");
            }
            if (StrUtil.isBlank(mongoDatabase)) {
                throw new ValidateException("mongo database is required");
            }
            if (StrUtil.isBlank(mongoCollection)) {
                throw new ValidateException("mongo collection is required");
            }
        }
    }
}
