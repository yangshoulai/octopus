package com.octopus.core.replay;

import com.octopus.core.Request;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/1/11
 */
public interface ReplayFilter {

  boolean filter(Request request);

}
