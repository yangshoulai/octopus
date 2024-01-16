package com.octopus.core.processor;

import com.octopus.core.Request;
import lombok.Getter;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
@Getter
class Result<T> {

    private T obj;

    private List<Request> requests;

    public Result() {
    }

    public Result(T result, List<Request> requests) {
        this.obj = result;
        this.requests = requests;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }
}
