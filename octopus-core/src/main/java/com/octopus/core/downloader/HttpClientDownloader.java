package com.octopus.core.downloader;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.net.url.UrlBuilder;
import com.octopus.core.Request;
import com.octopus.core.Request.RequestMethod;
import com.octopus.core.Response;
import com.octopus.core.downloader.proxy.ProxyProvider;
import com.octopus.core.exception.DownloadException;
import com.octopus.core.exception.OctopusException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class HttpClientDownloader implements Downloader {

  private final Logger log = LoggerFactory.getLogger("HttpClientDownloader");

  private HttpClient httpClient;

  public HttpClientDownloader() {
    this.init();
  }

  private void init() {
    SSLContext sslContext;
    try {
      sslContext = new SSLContextBuilder().loadTrustMaterial((chain, authType) -> true).build();
    } catch (Exception e) {
      throw new OctopusException(e);
    }
    Registry<ConnectionSocketFactory> socketFactoryRegistry =
        RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE)
            .register(
                "https", new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()))
            .build();
    PoolingHttpClientConnectionManager connectionManager =
        new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    connectionManager.setMaxTotal(200);
    connectionManager.setDefaultMaxPerRoute(25);
    connectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(3000).build());
    this.httpClient =
        HttpClients.custom()
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .setConnectionManager(connectionManager)
            .build();
  }

  @Override
  public Response download(Request request, DownloadConfig config) throws DownloadException {
    Proxy proxy = this.resolveProxy(config.getProxyProvider(), request);
    HttpResponse httpResponse;
    try {
      log.debug("Fetch [{}] via proxy [{}]", request, proxy);
      HttpUriRequest http = createHttpUriRequest(request, proxy, config);
      httpResponse = this.httpClient.execute(http);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      Response response = new Response(request);
      response.setStatus(statusCode);
      Header[] allHeaders = httpResponse.getAllHeaders();
      Map<String, String> headers = new HashMap<>();
      Arrays.stream(allHeaders).forEach(header -> headers.put(header.getName(), header.getValue()));
      response.setHeaders(headers);
      HttpEntity entity = httpResponse.getEntity();

      if (entity != null) {
        response.setBody(EntityUtils.toByteArray(entity));
        Header header = entity.getContentType();
        Charset charset = null;
        String mimeType = null;
        if (header != null && header.getValue() != null) {
          ContentType contentType = ContentType.parse(header.getValue());
          charset = contentType.getCharset();
          mimeType = contentType.getMimeType();
        }
        charset =
            Stream.of(charset, config.getCharset())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        response.setCharset(charset);
        response.setMimeType(mimeType);
      }
      return response;
    } catch (IOException e) {
      throw new DownloadException(
          String.format(
              "Fetch [%s] via proxy [%s] failed, caused by %s", request, proxy, e.getMessage()),
          e);
    }
  }

  private Proxy resolveProxy(ProxyProvider proxyProvider, Request request) {
    Proxy proxy = null;
    if (proxyProvider != null) {
      proxy = proxyProvider.provide(request);
    }
    return proxy == null ? Proxy.NO_PROXY : proxy;
  }

  private HttpUriRequest createHttpUriRequest(Request request, Proxy proxy, DownloadConfig config) {
    HttpRequestBase http;
    UrlBuilder urlBuilder = UrlBuilder.ofHttpWithoutEncode(request.getUrl());
    if (request.getParams() != null) {
      request.getParams().forEach(urlBuilder::addQuery);
    }
    String url = urlBuilder.build();
    if (request.getMethod() == RequestMethod.POST) {
      HttpPost post = new HttpPost(url);
      if (request.getBody() != null) {
        post.setEntity(new ByteArrayEntity(request.getBody()));
      }
      http = post;
    } else {
      http = new HttpGet(url);
    }

    Map<String, String> allHeaders =
        new MapBuilder<>(config.getHeaders()).putAll(request.getHeaders()).build();
    allHeaders.entrySet().stream()
        .map(pair -> new BasicHeader(pair.getKey(), pair.getValue()))
        .forEach(http::setHeader);
    RequestConfig.Builder builder =
        RequestConfig.custom()
            .setConnectionRequestTimeout(config.getConnectTimeout())
            .setConnectTimeout(config.getConnectTimeout())
            .setSocketTimeout(config.getSocketTimeout())
            .setCookieSpec(CookieSpecs.STANDARD);

    if (proxy != null && proxy != Proxy.NO_PROXY) {
      InetSocketAddress address = (InetSocketAddress) proxy.address();
      builder.setProxy(
          new HttpHost(address.getHostName(), address.getPort(), proxy.type().toString()));
    }
    http.setConfig(builder.build());
    return http;
  }
}
