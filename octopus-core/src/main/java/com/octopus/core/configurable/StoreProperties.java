package com.octopus.core.configurable;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.store.*;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import lombok.Data;

/**
 * 请求存储器配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class StoreProperties implements Validatable, Transformable<Store> {

    /**
     * 类型
     * <p>
     * 默认 Memory
     */
    private StoreType type = StoreType.Memory;

    /**
     * Redis 存储器 主机
     * <p>
     * 默认 127.0.0.1
     */
    private String redisHost = "127.0.0.1";

    /**
     * Redis 存储器 端口
     * <p>
     * 默认 6379
     */
    private int redisPort = 6379;
    /**
     * Redis 存储器 存储健前缀
     * <p>
     * 默认 octopus
     */
    private String redisKeyPrefix = "octopus";

    /**
     * Sqlite 存储器 数据库文件
     * <p>
     * 默认 空
     */
    private String sqliteDatabaseFilePath;

    /**
     * Sqlite 存储器 数据库表名
     * <p>
     * 默认 requests
     */
    private String sqliteTableName = "requests";


    /**
     * Mongo 存储器 主机
     * <p>
     * 默认 127.0.0.1
     */
    private String mongoHost = "127.0.0.1";

    /**
     * Mongo 存储器 端口
     * <p>
     * 默认 27017
     */
    private int mongoPort = 27017;

    /**
     * Mongo 存储器 数据库名称
     * <p>
     * 默认 octopus
     */
    private String mongoDatabase = "octopus";

    /**
     * Mongo 存储器 数据库集合名称
     * <p>
     * 默认 request
     */
    private String mongoCollection = "request";


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

        if (type == StoreType.Sqlite) {
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

    @Override
    public Store transform() {
        switch (type) {
            case Redis:
                return new RedisStore(redisKeyPrefix, redisHost, redisPort);
            case Sqlite:
                return new SQLiteStore(sqliteDatabaseFilePath, sqliteTableName);
            case Memory:
                return new MemoryStore();
            case Mongo:
                return new MongoStore(mongoDatabase, mongoCollection, mongoHost, mongoPort);
            default:
                return null;
        }
    }
}
