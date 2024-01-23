package com.octopus.sample.music;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Response;
import com.octopus.core.WebSite;
import com.octopus.core.processor.impl.MediaFileDownloadProcessor;
import com.octopus.core.utils.AvgRateLimiter;
import com.octopus.sample.Constants;

/**
 * 下载网易云音乐热歌榜歌曲
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
public class NetEaseMusic {

  public static void main(String[] args) {
    Octopus.builder()
        .addSeeds("https://music.163.com/discover/toplist?id=3778678")
        .addSite(WebSite.of("music.163.com").setRateLimiter(AvgRateLimiter.of(1, 5)))
        .addProcessor(new ListPageProcessor())
        .addProcessor(new PlayerUrlProcessor())
        .addProcessor(
            new MediaFileDownloadProcessor(FileUtil.file(Constants.DOWNLOAD_DIR + "/music")) {
              @Override
              protected String resolveSaveName(Response response) {
                String suffix = FileUtil.getSuffix(response.getRequest().getUrl());
                return response.getRequest().getAttribute("name")
                    + (StrUtil.isBlank(suffix) ? ".mp3" : "." + suffix);
              }
            })
        .build()
        .start();
  }
}
