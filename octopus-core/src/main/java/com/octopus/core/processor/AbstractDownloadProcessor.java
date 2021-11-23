package com.octopus.core.processor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.processor.matcher.Matcher;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public abstract class AbstractDownloadProcessor extends AbstractProcessor {

  private static final Pattern FILE_NAME_PATTERN = Pattern.compile(".*(\\..*)");

  public AbstractDownloadProcessor(@NonNull Matcher matcher) {
    super(matcher);
  }

  @Override
  public List<Request> process(Response response) {
    File downloadDir = this.resolveSaveDir(response);
    String fileName = this.resolveSaveName(response);
    try {
      FileUtil.writeBytes(response.getBody(), new File(downloadDir, fileName));
    } catch (Exception e) {
      throw new ProcessException(
          String.format("Can not save file [%s]", FileUtil.file(downloadDir, fileName)), e);
    }
    return null;
  }

  /**
   * 获取文件保存目录
   *
   * @param response 下载响应
   * @return 保存目录
   */
  protected abstract File resolveSaveDir(Response response);

  /**
   * 获取文件保存名称
   *
   * @param response 下载响应
   * @return 文件名称
   */
  protected String resolveSaveName(Response response) {
    String fileName = getFileNameFromDisposition(response.getHeaders());
    if (StrUtil.isBlank(fileName)) {
      String path = URLUtil.url(URLUtil.normalize(response.getRequest().getUrl())).getPath();
      fileName = StrUtil.subSuf(path, path.lastIndexOf("/") + 1);
      if (StrUtil.isBlank(fileName)) {
        fileName = URLUtil.encodeQuery(path, CharsetUtil.CHARSET_UTF_8);
      }
    }
    File file = new File(this.resolveSaveDir(response), fileName);
    String path = file.getPath();
    if (path.length() > 255) {
      java.util.regex.Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
      fileName = IdUtil.simpleUUID();
      if (ReUtil.isMatch(FILE_NAME_PATTERN, fileName)) {
        String postfix = ReUtil.getGroup1(FILE_NAME_PATTERN, fileName);
        fileName = fileName + postfix;
      }
    }
    return fileName;
  }

  /**
   * 从Content-Disposition头中获取文件名
   *
   * @return 文件名，empty表示无
   */
  private String getFileNameFromDisposition(Map<String, String> headers) {
    String fileName = null;
    if (headers != null) {
      final String disposition = headers.get("Content-Disposition");
      if (StrUtil.isNotBlank(disposition)) {
        fileName = ReUtil.get("filename=\"(.*?)\"", disposition, 1);
        if (StrUtil.isBlank(fileName)) {
          fileName = StrUtil.subAfter(disposition, "filename=", true);
        }
      }
    }
    return fileName;
  }
}
