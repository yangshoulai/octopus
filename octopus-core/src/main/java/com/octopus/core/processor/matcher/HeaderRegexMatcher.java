package com.octopus.core.processor.matcher;

import cn.hutool.core.util.ReUtil;
import com.octopus.core.Response;
import java.util.regex.Pattern;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class HeaderRegexMatcher implements Matcher {

  private final String headerName;

  private final Pattern pattern;

  public HeaderRegexMatcher(@NonNull String headerName, @NonNull Pattern pattern) {
    this.headerName = headerName;
    this.pattern = pattern;
  }

  public HeaderRegexMatcher(@NonNull String headerName, @NonNull String regex) {
    this.headerName = headerName;
    this.pattern = Pattern.compile(regex);
  }

  @Override
  public boolean matches(Response response) {
    String value = response.getHeaders().get(headerName);
    return ReUtil.isMatch(this.pattern, value);
  }
}
