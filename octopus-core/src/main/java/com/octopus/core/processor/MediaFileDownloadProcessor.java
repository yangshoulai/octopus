package com.octopus.core.processor;

import com.octopus.core.processor.matcher.Matchers;
import java.io.File;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class MediaFileDownloadProcessor extends DefaultDownloadProcessor {

  public MediaFileDownloadProcessor(@NonNull File saveDirectory) {
    super(
        Matchers.or(Matchers.image(), Matchers.audio(), Matchers.video(), Matchers.octetStream()),
        saveDirectory);
  }
}
