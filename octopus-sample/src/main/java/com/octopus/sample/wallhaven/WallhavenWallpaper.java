package com.octopus.sample.wallhaven;

import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher;
import com.octopus.core.processor.extractor.annotation.ExtractorMatcher.Type;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.selector.Selector;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 下载壁纸天堂壁纸 https://wallhaven.cc
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
@Slf4j
@Data
@Extractor(matcher = @ExtractorMatcher(type = Type.HTML))
@Link(selectors = @Selector(value = "img#wallpaper", attr = "src"))
@Link(selectors = @Selector(value = "#thumbs .thumb-listing-page ul li a.preview", attr = "href"))
@Link(selectors = @Selector(value = "ul.pagination li a.next", attr = "href"))
public class WallhavenWallpaper {

  /** 壁纸列表 */
  @Selector(value = "#thumbs .thumb-listing-page ul li")
  private List<Wallpaper> wallpapers;

  /** 壁纸数据 */
  @Data
  @Extractor
  public static class Wallpaper {

    /** 壁纸图片链接 */
    @Selector(value = "img", attr = "data-src")
    private String src;

    /** 壁纸预览图链接 */
    @Selector(value = "a.preview", attr = "href")
    private String previewSrc;

    /** 壁纸分辨率 */
    @Selector(value = ".wall-res")
    private String resolution;
  }
}
