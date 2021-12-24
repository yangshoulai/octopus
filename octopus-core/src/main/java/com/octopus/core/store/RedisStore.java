package com.octopus.core.store;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Request;
import com.octopus.core.Request.State;
import com.octopus.core.Request.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
      Set<String> idSet = jedis.zrevrange(this.waitingKey, 0, 0);
      if (!idSet.isEmpty()) {
        String selected = idSet.stream().findFirst().get();
        try (Transaction transaction = jedis.multi()) {
          transaction.sadd(this.executingKey, selected);
          transaction.zrem(this.waitingKey, selected);
          transaction.exec();
        }
        if (StrUtil.isNotBlank(selected)) {
          String json = jedis.hget(this.allKey, selected);
          Request request = JSONUtil.toBean(json, Request.class);
          request.setStatus(Status.of(State.Executing));
          return request;
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
  public void markAsFailed(Request request, String error) {
    try (Jedis jedis = this.pool.getResource()) {
      jedis.hset(this.failedKey, request.getId(), error);
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

  @Override
  public List<Request> getFailed() {
    List<Request> failed = new ArrayList<>();
    try (Jedis jedis = this.pool.getResource()) {
      Map<String, String> fails = jedis.hgetAll(this.failedKey);
      fails.forEach(
          (id, error) -> {
            String json = jedis.hget(this.allKey, id);
            Request request = JSONUtil.toBean(json, Request.class);
            request.setStatus(Status.of(State.Failed, error));
            failed.add(request);
          });
    }
    return failed;
  }

  @Override
  public void clearFailed() {
    try (Jedis jedis = this.pool.getResource()) {
      for (String id : jedis.hkeys(this.failedKey)) {
        jedis.hdel(this.allKey, id);
      }
      jedis.del(this.failedKey);
    }
  }
}
