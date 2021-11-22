package com.octopus.core.processor.matcher;

import cn.hutool.core.util.ReUtil;
import com.octopus.core.Response;
import java.util.regex.Pattern;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class UrlRegexMatcher implements Matcher {

  private final Pattern pattern;

  public UrlRegexMatcher(@NonNull Pattern pattern) {
    this.pattern = pattern;
  }

  public UrlRegexMatcher(@NonNull String regex) {
    this.pattern = Pattern.compile(regex);
  }

  @Override
  public boolean matches(Response response) {
    return ReUtil.isMatch(this.pattern, response.getRequest().getUrl());
  }
}
