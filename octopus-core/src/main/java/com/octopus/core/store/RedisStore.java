package com.octopus.core.store;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Request;
import java.util.Set;
import lombok.NonNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
public class RedisStore implements Store {

  private final JedisPool pool;

  private final String allKey;

  private final String waitingKey;

  private final String completedKey;

  private final String failedKey;

  private final String executingKey;

  public RedisStore(@NonNull String keyPrefix, @NonNull JedisPool pool) {
    this.pool = pool;
    this.allKey = keyPrefix + "-" + "all";
    this.waitingKey = keyPrefix + "-" + "waiting";
    this.completedKey = keyPrefix + "-" + "completed";
    this.failedKey = keyPrefix + "-" + "failed";
    this.executingKey = keyPrefix + "-" + "executing";
    try (Jedis jedis = this.pool.getResource()) {
      Set<String> executing = jedis.smembers(this.executingKey);
      if (!executing.isEmpty()) {
        executing.forEach(
            id -> {
              if (jedis.zscore(this.waitingKey, id) == null) {
                Request request = JSONUtil.toBean(jedis.hget(this.allKey, id), Request.class);
                jedis.zadd(this.waitingKey, request.getPriority(), id);
                jedis.srem(this.executingKey, id);
              }
            });
      }
    }
  }

  public RedisStore(@NonNull JedisPool pool) {
    this("octopus", pool);
  }

  public RedisStore(@NonNull String host, int port) {
    this(new JedisPool(host, port));
  }

  public RedisStore() {
    this(new JedisPool());
  }

  @Override
  public Request get() {
    try (Jedis jedis = this.pool.getResource()) {
      Set<String> idSet = jedis.zrevrange(this.waitingKey, 0, 1);
      if (!idSet.isEmpty()) {
        String id = ListUtil.toList(idSet).get(0);
        try (Transaction transaction = jedis.multi()) {
          transaction.sadd(this.executingKey, id);
          transaction.zrem(this.waitingKey, id);
          transaction.exec();
        }
        if (StrUtil.isNotBlank(id)) {
          String json = jedis.hget(this.allKey, id);
          return JSONUtil.toBean(json, Request.class);
        }
      }
    }
    return null;
  }

  @Override
  public boolean put(Request request) {
    try (Jedis jedis = this.pool.getResource()) {
      jedis.hset(this.allKey, request.getId(), JSONUtil.toJsonStr(request));
      jedis.zadd(this.waitingKey, request.getPriority(), request.getId());
      return true;
    }
  }

  @Override
  public boolean exists(Request request) {
    try (Jedis jedis = this.pool.getResource()) {
      return jedis.hexists(this.allKey, request.getId());
    }
  }

  @Override
  public void clear() {
    try (Jedis jedis = this.pool.getResource()) {
      jedis.del(this.allKey, this.waitingKey, this.completedKey, this.executingKey, this.failedKey);
    }
  }

  @Override
  public void markAsCompleted(Request request) {
    try (Jedis jedis = this.pool.getResource()) {
      jedis.sadd(this.completedKey, request.getId());
      jedis.srem(this.executingKey, request.getId());
    }
  }

  @Override
  public void markAsFailed(Request request) {
    try (Jedis jedis = this.pool.getResource()) {
      jedis.sadd(this.failedKey, request.getId());
      jedis.srem(this.executingKey, request.getId());
    }
  }

  @Override
  public long getTotalSize() {
    try (Jedis jedis = this.pool.getResource()) {
      return jedis.hlen(this.allKey);
    }
  }

  @Override
  public long getCompletedSize() {
    try (Jedis jedis = this.pool.getResource()) {
      return jedis.scard(this.completedKey);
    }
  }

  @Override
  public long getWaitingSize() {
    try (Jedis jedis = this.pool.getResource()) {
      return jedis.zcard(this.waitingKey);
    }
  }
}
