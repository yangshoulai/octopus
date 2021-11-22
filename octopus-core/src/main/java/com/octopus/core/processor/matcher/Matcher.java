package com.octopus.core.processor.matcher;

import com.octopus.core.Response;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public interface Matcher {

  boolean matches(Response response);
}
