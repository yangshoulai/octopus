package com.octopus.core.extractor;

import com.octopus.core.Request;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/26
 */
public class ExtractResult<T> {

  private T obj;

  private List<Request> requests;

  public ExtractResult() {
  }

  public ExtractResult(T result, List<Request> requests) {
    this.obj = result;
    this.requests = requests;
  }

  public T getObj() {
    return obj;
  }

  public void setObj(T obj) {
    this.obj = obj;
  }

  public List<Request> getRequests() {
    return requests;
  }

  public void setRequests(List<Request> requests) {
    this.requests = requests;
  }
}
