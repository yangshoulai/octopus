package com.octopus.sample.wallhaven;

import com.octopus.core.extractor.annotation.Extractor;
import com.octopus.core.extractor.annotation.Link;
import com.octopus.core.extractor.annotation.Selector;
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
@Extractor
@Link(selector = @Selector(expression = "img#wallpaper", attr = "src"))
@Link(
    selector = @Selector(expression = "#thumbs .thumb-listing-page ul li a.preview", attr = "href"))
@Link(selector = @Selector(expression = "ul.pagination li a.next", attr = "href", multi = false))
public class WallhavenWallpaper {

  /** 壁纸列表 */
  @Selector(expression = "#thumbs .thumb-listing-page ul li")
  private List<Wallpaper> wallpapers;

  /** 壁纸数据 */
  @Data
  @Extractor
  public static class Wallpaper {

    /** 壁纸图片链接 */
    @Selector(expression = "img", attr = "data-src")
    private String src;

    /** 壁纸预览图链接 */
    @Selector(expression = "a.preview", attr = "href")
    private String previewSrc;

    /** 壁纸分辨率 */
    @Selector(expression = ".wall-res")
    private String resolution;
  }
}
