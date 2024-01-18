package com.octopus.core.properties.store;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.store.SqliteStore;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/17
 */
@Data
public class SqliteStoreProperties implements Validatable, Transformable<SqliteStore> {

    public static final String DEFAULT_TABLE_NAME = "requests";

    /**
     * Sqlite 存储器 数据库文件
     * <p>
     * 默认 空
     */
    private String db;

    /**
     * Sqlite 存储器 数据库表名
     * <p>
     * 默认 requests
     */
    private String table = DEFAULT_TABLE_NAME;

    public SqliteStoreProperties() {
    }

    public SqliteStoreProperties(@NonNull String db) {
        this.db = db;
    }

    public SqliteStoreProperties(@NonNull String db, @NonNull String table) {
        this.db = db;
        this.table = table;
    }

    @Override
    public SqliteStore transform() {
        return new SqliteStore(this);
    }

    @Override
    public void validate() throws ValidateException {
        Validator.notBlank(db, "sqlite db is required");
        Validator.notBlank(db, "sqlite table is required");
    }
}
