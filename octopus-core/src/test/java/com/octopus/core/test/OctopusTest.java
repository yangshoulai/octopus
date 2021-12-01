package com.octopus.core.test;

import com.octopus.core.Octopus;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class OctopusTest {

  public static void main(String[] args) {
    Octopus.builder().addSeeds("https://wwww.baidu.com").build().start();
  }
}
