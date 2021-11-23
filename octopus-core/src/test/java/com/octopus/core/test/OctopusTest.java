package com.octopus.core.test;

import com.octopus.core.Octopus;
import com.octopus.core.Request;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class OctopusTest {

  public static void main(String[] args) throws InterruptedException {
    Octopus octopus = Octopus.builder().autoStop(true).build();
    octopus.addRequest(Request.get("http://www.baidu.com"));
    octopus.startAsync();
  }
}
