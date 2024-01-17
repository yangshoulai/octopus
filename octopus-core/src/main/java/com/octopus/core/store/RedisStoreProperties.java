package com.octopus.core.store;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.store.RedisStore;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import lombok.Data;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/17
 */
@Data
public class RedisStoreProperties implements Validatable, Transformable<RedisStore> {

    public static final String DEFAULT_URI = "redis://127.0.0.1:6379";

    public static final String DEFAULT_PREFIX = "octopus";

    /**
     * Redis 存储器 链接
     * <p>
     * 默认 redis://127.0.0.1:6379
     */
    private String uri = DEFAULT_URI;
    /**
     * Redis 存储器 存储键前缀
     * <p>
     * 默认 octopus
     */
    private String prefix = DEFAULT_PREFIX;

    public RedisStoreProperties() {
    }

    public RedisStoreProperties(@NonNull String uri, @NonNull String prefix) {
        this.uri = uri;
        this.prefix = prefix;
    }

    @Override
    public void validate() throws ValidateException {
        if (StrUtil.isBlank(uri)) {
            throw new ValidateException("redis uri is required");
        }

        if (StrUtil.isBlank(prefix)) {
            throw new ValidateException("redis key prefix is required");
        }
    }

    @Override
    public RedisStore transform() {
        return new RedisStore(this);
    }
}
