package com.octopus.core.processor.impl;

import com.octopus.core.Response;
import com.octopus.core.processor.matcher.Matcher;
import java.io.File;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class DefaultDownloadProcessor extends AbstractDownloadProcessor {

  private final File saveDirectory;

  public DefaultDownloadProcessor(Matcher matcher, @NonNull File saveDirectory) {
    super(matcher);
    this.saveDirectory = saveDirectory;
  }

  @Override
  public File resolveSaveDir(Response response) {
    return saveDirectory;
  }
}
