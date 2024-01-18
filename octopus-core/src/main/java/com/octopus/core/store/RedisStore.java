package com.octopus.core.store;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.Request.State;
import com.octopus.core.Request.Status;
import com.octopus.core.properties.store.RedisStoreProperties;
import com.octopus.core.replay.ReplayFilter;
import lombok.NonNull;
import org.redisson.Redisson;
import org.redisson.api.*;
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

    private final RedissonClient redissonClient;

    private RScoredSortedSet<String> waiting;

    private RSet<String> executing;

    private RSet<String> completed;

    private RMap<String, Request> all;

    private RMap<String, String> fails;

    private RLock lock;


    public RedisStore(@NonNull RedisStoreProperties properties) {
        Config config = new Config();
        config.useSingleServer().setAddress(properties.getUri());
        this.redissonClient = Redisson.create(config);
        this.init(properties.getPrefix());
    }


    private void init(String prefix) {
        this.lock = this.redissonClient.getLock(prefix + ":lock");
        this.all = this.redissonClient.getMap(prefix + ":all");
        this.fails = this.redissonClient.getMap(prefix + ":fails");
        this.waiting = this.redissonClient.getScoredSortedSet(prefix + ":waiting");
        this.completed = this.redissonClient.getSet(prefix + ":completed");
        this.executing = this.redissonClient.getSet(prefix + ":executing");
        try {
            this.lock.lock();
            if (!this.executing.isEmpty()) {
                for (String id : executing) {
                    if (!this.waiting.contains(id)) {
                        Request request = this.all.get(id);
                        this.waiting.add(request.getPriority(), id);
                        this.executing.remove(id);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Request get() {
        String id = this.waiting.pollFirst();
        if (StrUtil.isNotBlank(id)) {
            this.executing.add(id);
            return this.all.get(id);
        }
        return null;
    }

    @Override
    public boolean put(Request request) {
        this.all.put(request.getId(), request);
        this.waiting.add(request.getPriority(), request.getId());
        return true;
    }

    @Override
    public boolean exists(Request request) {
        return this.all.containsKey(request.getId());
    }

    @Override
    public void clear() {
        this.all.clear();
        this.waiting.clear();
        this.completed.clear();
        this.executing.clear();
        this.fails.clear();
    }

    @Override
    public void markAsCompleted(Request request) {
        this.completed.add(request.getId());
        this.executing.remove(request.getId());
        request.setStatus(Status.of(State.Completed));
        this.all.put(request.getId(), request);
    }

    @Override
    public void markAsFailed(Request request, String error) {
        this.fails.put(request.getId(), error);
        this.executing.remove(request.getId());
        request.setStatus(Status.of(State.Failed, error));
        request.setFailTimes(request.getFailTimes() + 1);
        this.all.put(request.getId(), request);
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
        this.all.remove(id);
        this.fails.remove(id);
        this.waiting.remove(id);
        this.completed.remove(id);
    }

    @Override
    public int replayFailed(ReplayFilter filter) {
        List<String> keys = new ArrayList<>();
        for (String id : this.fails.keySet()) {
            Request request = this.all.get(id);
            if (filter.filter(request)) {
                request.setStatus(Status.of(State.Waiting));
                this.all.put(id, request);
                this.waiting.add(request.getPriority(), id);
                keys.add(id);
            }
        }

        for (String id : keys) {
            this.fails.remove(id);
        }
        return keys.size();
    }

}
