package com.octopus.core.listener;

import com.octopus.core.Request;
import com.octopus.core.Response;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class AttributeInheritListener implements Listener {

  @Override
  public void afterProcess(Response response, List<Request> newRequests) {
    if (newRequests != null) {
      newRequests.forEach(request -> request.putAttributes(response.getRequest().getAttributes()));
    }
  }
}
