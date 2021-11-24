package com.octopus.core.processor;

import cn.hutool.core.io.FileUtil;
import com.octopus.core.processor.matcher.Matchers;
import java.io.File;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class MediaFileDownloadProcessor extends DefaultDownloadProcessor {

  public MediaFileDownloadProcessor(@NonNull String saveDirectory) {
    this(FileUtil.file(saveDirectory));
  }

  public MediaFileDownloadProcessor(@NonNull File saveDirectory) {
    super(
        Matchers.or(Matchers.IMAGE, Matchers.AUDIO, Matchers.VIDEO, Matchers.OCTET_STREAM),
        saveDirectory);
  }
}
