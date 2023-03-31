package com.octopus.core;

import com.octopus.core.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/31
 */
class OctopusListenerNotifier implements OctopusListener {

  private final Logger logger;

  private final List<OctopusListener> listeners = new ArrayList<>();

  public OctopusListenerNotifier(@NonNull List<OctopusListener> listeners, @NonNull Logger logger) {
    this.listeners.addAll(listeners);
    this.logger = logger;
  }

  @Override
  public void beforeStore(Request request) {
    traceMethod("beforeStore", request);
    for (OctopusListener listener : this.listeners) {
      try {
        traceMethod(listener, "beforeStore");
        listener.beforeStore(request);
      } catch (Throwable e) {
        errorMethod(listener, "beforeStore", e);
      }
    }
  }

  @Override
  public void beforeDownload(Request request) {
    traceMethod("beforeDownload", request);
    for (OctopusListener listener : this.listeners) {
      try {
        traceMethod(listener, "beforeDownload");
        listener.beforeDownload(request);
      } catch (Throwable e) {
        errorMethod(listener, "beforeDownload", e);
      }
    }
  }

  @Override
  public void onError(Request request, Throwable e) {
    traceMethod("onError", request);
    for (OctopusListener listener : this.listeners) {
      try {
        traceMethod(listener, "onError");
        listener.onError(request, e);
      } catch (Throwable t) {
        errorMethod(listener, "onError", t);
      }
    }
  }

  @Override
  public void beforeProcess(Response response) {
    traceMethod("beforeProcess", response.getRequest());
    for (OctopusListener listener : this.listeners) {
      try {
        traceMethod(listener, "beforeProcess");
        listener.beforeProcess(response);
      } catch (Throwable e) {
        errorMethod(listener, "beforeProcess", e);
      }
    }
  }

  @Override
  public void afterProcess(Response response) {
    traceMethod("afterProcess", response.getRequest());
    for (OctopusListener listener : this.listeners) {
      try {
        traceMethod(listener, "afterProcess");
        listener.afterProcess(response);
      } catch (Throwable e) {
        errorMethod(listener, "afterProcess", e);
      }
    }
  }

  private void traceMethod(String method, Request request) {
    if (logger.isTraceEnabled()) {
      logger.trace(
          String.format("Invoke method [%s] on listeners with request [%s]", method, request));
    }
  }

  private void traceMethod(OctopusListener listener, String method) {
    if (logger.isTraceEnabled()) {
      logger.trace(
          String.format(
              "Invoke method [%s] on listener [%s]", method, listener.getClass().getName()));
    }
  }

  private void errorMethod(OctopusListener listener, String method, Throwable e) {
    logger.error(
        String.format(
            "Ignore error when invoke method [%s] on listener [%s]",
            method, listener.getClass().getName()),
        e);
  }
}
