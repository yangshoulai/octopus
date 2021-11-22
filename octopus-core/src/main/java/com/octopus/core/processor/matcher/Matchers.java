package com.octopus.core.processor.matcher;

import cn.hutool.core.collection.ListUtil;
import java.util.regex.Pattern;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class Matchers {

  public static Matcher and(@NonNull Matcher... matchers) {
    return new AndMatcher(ListUtil.of(matchers));
  }

  public static Matcher or(@NonNull Matcher... matchers) {
    return new OrMatcher(ListUtil.of(matchers));
  }

  public static Matcher urlRegex(@NonNull Pattern pattern) {
    return new UrlRegexMatcher(pattern);
  }

  public static Matcher urlRegex(@NonNull String regex) {
    return new UrlRegexMatcher(regex);
  }

  public static Matcher headerRegex(@NonNull String header, @NonNull Pattern pattern) {
    return new HeaderRegexMatcher(header, pattern);
  }

  public static Matcher headerRegex(@NonNull String header, @NonNull String regex) {
    return new HeaderRegexMatcher(header, regex);
  }

  public static Matcher contentType(@NonNull String regex) {
    return new ContentTypeMatcher(regex);
  }

  public static Matcher contentType(@NonNull Pattern pattern) {
    return new ContentTypeMatcher(pattern);
  }

  public static Matcher json() {
    return contentType(".*application/json.*");
  }

  public static Matcher image() {
    return contentType(".*image/.*");
  }

  public static Matcher video() {
    return contentType(".*video/.*");
  }

  public static Matcher audio() {
    return contentType(".*audio/.*");
  }

  public static Matcher html() {
    return contentType(".*text/html.*");
  }

  public static Matcher octetStream() {
    return contentType(".*application/octet-stream.*");
  }

  public static Matcher all() {
    return r -> true;
  }
}
