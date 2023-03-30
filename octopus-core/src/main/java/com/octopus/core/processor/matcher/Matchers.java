package com.octopus.core.processor.matcher;

import cn.hutool.core.collection.ListUtil;
import java.util.regex.Pattern;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public class Matchers {

  public static final Matcher ALL = r -> true;

  public static final Matcher JSON = contentType(".*application/json.*");

  public static final Matcher IMAGE = contentType(".*image/.*");

  public static final Matcher VIDEO = contentType(".*video/.*");

  public static final Matcher AUDIO = contentType(".*audio/.*");

  public static final Matcher HTML = contentType(".*text/html.*");

  public static final Matcher OCTET_STREAM = contentType(".*application/octet-stream.*");

  public static final Matcher MEDIA =
      Matchers.or(Matchers.IMAGE, Matchers.VIDEO, Matchers.AUDIO, Matchers.OCTET_STREAM);

  public static Matcher and(@NonNull Matcher... matchers) {
    return new AndMatcher(ListUtil.of(matchers));
  }

  public static Matcher or(@NonNull Matcher... matchers) {
    return new OrMatcher(ListUtil.of(matchers));
  }

  public static Matcher not(@NonNull Matcher matcher) {
    return new NotMatcher(matcher);
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
}
