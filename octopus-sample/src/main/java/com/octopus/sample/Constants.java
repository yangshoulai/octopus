package com.octopus.sample;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/30
 */
public class Constants {

  public static final String DOWNLOAD_DIR = "~/Downloads";

  public static final Proxy PROXY =
      new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 17890));
}
