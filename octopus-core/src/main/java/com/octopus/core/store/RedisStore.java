package com.octopus.core.store;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.octopus.core.Request;
import com.octopus.core.Request.State;
import com.octopus.core.Request.Status;
import com.octopus.core.Response;
import com.octopus.core.properties.store.RedisStoreProperties;
import com.octopus.core.replay.ReplayFilter;
import lombok.NonNull;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 存储器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
public class RedisStore implements Store {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private final RedissonClient redissonClient;

    private RScoredSortedSet<String> waiting;

    private RSet<String> executing;

    private RSet<String> completed;

    private RMap<String, Response> responses;

    private RMap<String, Request> all;

    private RMap<String, String> fails;

    private String waitingKey;

    private String completedKey;

    private String allKey;

    private String executingKey;

    private String failsKey;

    private String responsesKey;


    public RedisStore(@NonNull RedisStoreProperties properties) {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec(MAPPER));
        config.useSingleServer().setAddress(properties.getUri());
        this.redissonClient = Redisson.create(config);
        this.init(properties.getPrefix());
    }

    private void init(String prefix) {
        this.allKey = prefix + ":all";
        this.failsKey = prefix + ":fails";
        this.waitingKey = prefix + ":waiting";
        this.completedKey = prefix + ":completed";
        this.executingKey = prefix + ":executing";
        this.responsesKey = prefix + ":responses";

        this.responses = this.redissonClient.getMap(this.responsesKey);
        this.all = this.redissonClient.getMap(this.allKey);
        this.fails = this.redissonClient.getMap(this.failsKey);
        this.waiting = this.redissonClient.getScoredSortedSet(this.waitingKey);
        this.completed = this.redissonClient.getSet(this.completedKey);
        this.executing = this.redissonClient.getSet(this.executingKey);
        RBatch batch = this.redissonClient.createBatch();
        for (String id : this.executing) {
            Request request = this.all.get(id);
            batch.<String>getScoredSortedSet(this.waitingKey).addAsync(request.getPriority(), request.getId());
            batch.<String>getSet(this.executingKey).removeAsync(id);
        }
        batch.execute();
    }

    @Override
    public Request get() {
        String id = this.waiting.last();
        if (StrUtil.isNotBlank(id)) {
            Request request = this.all.get(id);
            RBatch batch = this.redissonClient.createBatch();
            batch.<String>getSet(this.executingKey).addAsync(id);
            batch.<String>getScoredSortedSet(this.waitingKey).removeAsync(id);
            request.setStatus(Status.of(State.Waiting));
            batch.<String, Request>getMap(this.allKey).putAsync(id, request);
            batch.execute();
            return request;
        }
        return null;
    }

    @Override
    public boolean put(Request request) {
        request.setStatus(Status.of(State.Waiting));
        RBatch batch = this.redissonClient.createBatch();
        batch.<String, Request>getMap(allKey).putAsync(request.getId(), request);
        batch.<String>getScoredSortedSet(this.waitingKey).addAsync(request.getPriority(), request.getId());
        this.all.put(request.getId(), request);
        this.waiting.add(request.getPriority(), request.getId());
        batch.execute();
        return true;
    }

    @Override
    public boolean exists(Request request) {
        return this.all.containsKey(request.getId());
    }

    @Override
    public void clear() {
        RBatch batch = this.redissonClient.createBatch();
        batch.getMap(this.allKey).deleteAsync();
        batch.getScoredSortedSet(this.waitingKey).deleteAsync();
        batch.getSet(this.completedKey).deleteAsync();
        batch.getSet(this.executingKey).deleteAsync();
        batch.getMap(this.failsKey).deleteAsync();
        batch.getMap(this.responsesKey).deleteAsync();
        batch.execute();
    }

    @Override
    public void markAsCompleted(Request request) {
        request.setStatus(Status.of(State.Completed));
        RBatch batch = this.redissonClient.createBatch();
        batch.<String>getSet(this.completedKey).addAsync(request.getId());
        batch.<String>getSet(this.executingKey).removeAsync(request.getId());
        batch.<String, Request>getMap(this.allKey).putAsync(request.getId(), request);
        batch.execute();
    }

    @Override
    public void markAsFailed(Request request, String error) {
        request.setStatus(Status.of(State.Failed, error));
        request.setFailTimes(request.getFailTimes() + 1);
        RBatch batch = this.redissonClient.createBatch();
        batch.<String, String>getMap(this.failsKey).putAsync(request.getId(), error);
        batch.<String>getSet(this.executingKey).removeAsync(request.getId());
        batch.<String, Request>getMap(this.allKey).putAsync(request.getId(), request);
        batch.execute();
    }

    @Override
    public long getTotalSize() {
        return this.all.size();
    }

    @Override
    public long getCompletedSize() {
        return this.completed.size();
    }

    @Override
    public long getWaitingSize() {
        return this.waiting.size();
    }

    @Override
    public long getFailedSize() {
        return this.fails.size();
    }


    @Override
    public void delete(String id) {
        RBatch batch = this.redissonClient.createBatch();
        batch.getMap(this.allKey).removeAsync(id);
        batch.getScoredSortedSet(this.waitingKey).removeAsync(id);
        batch.getSet(this.completedKey).removeAsync(id);
        batch.getSet(this.executingKey).removeAsync(id);
        batch.getMap(this.failsKey).removeAsync(id);
        batch.getMap(this.responsesKey).removeAsync(id);
        batch.execute();
    }

    @Override
    public int replayFailed(ReplayFilter filter) {
        List<String> keys = new ArrayList<>();
        RBatch batch = this.redissonClient.createBatch();
        for (String id : this.fails.keySet()) {
            Request request = this.all.get(id);
            if (filter.filter(request)) {
                request.setStatus(Status.of(State.Waiting));
                batch.<String, Request>getMap(this.allKey).putAsync(id, request);
                batch.<String>getScoredSortedSet(this.waitingKey).addAsync(request.getPriority(), id);
                keys.add(id);
            }
        }
        for (String id : keys) {
            batch.<String, String>getMap(this.failsKey).removeAsync(id);
        }
        batch.execute();
        return keys.size();
    }

    @Override
    public void cacheResponse(Response response) {
        responses.put(response.getRequest().getId(), response);
    }

    @Override
    public Response getResponse(String id) {
        return responses.get(id);
    }
}
