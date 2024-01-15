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
     * Redis 存储器 链接
     * <p>
     * 默认 redis://127.0.0.1:6379
     */
    private String redisUri = "redis://127.0.0.1:6379";
    /**
     * Redis 存储器 存储键前缀
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
     * 默认 mongodb://127.0.0.1:27017/octopus
     */
    private String mongoUri = "mongodb://127.0.0.1:27017/octopus";

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
            if (StrUtil.isBlank(redisUri)) {
                throw new ValidateException("redis uri is required");
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
            if (StrUtil.isBlank(mongoUri)) {
                throw new ValidateException("mongo uri is required");
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
                return new RedisStore(redisKeyPrefix, redisUri);
            case Sqlite:
                return new SqliteStore(sqliteDatabaseFilePath, sqliteTableName);
            case Memory:
                return new MemoryStore();
            case Mongo:
                return new MongoStore(mongoCollection, mongoUri);
            default:
                return null;
        }
    }
}
