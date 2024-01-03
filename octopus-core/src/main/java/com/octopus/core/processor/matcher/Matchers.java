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

    public static final Matcher JSON = contentTypeRegex(".*application/json.*");

    public static final Matcher IMAGE = contentTypeRegex(".*image/.*");

    public static final Matcher VIDEO = contentTypeRegex(".*video/.*");

    public static final Matcher PDF = contentTypeRegex(".*application/pdf.*");

    public static final Matcher WORD = contentTypeRegex(".*application/msword.*");
    public static final Matcher EXCEL = contentTypeRegex(".*application/vnd.*");
    public static final Matcher AUDIO = contentTypeRegex(".*audio/.*");

    public static final Matcher HTML = contentTypeRegex(".*text/html.*");

    public static final Matcher OCTET_STREAM = contentTypeRegex(".*application/octet-stream.*");

    public static final Matcher MEDIA =
            Matchers.or(Matchers.IMAGE, Matchers.VIDEO, Matchers.AUDIO, Matchers.PDF, Matchers.WORD, Matchers.EXCEL, Matchers.OCTET_STREAM);

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

    public static Matcher contentTypeRegex(@NonNull String regex) {
        return new ContentTypeMatcher(regex);
    }

    public static Matcher contentTypeRegex(@NonNull Pattern pattern) {
        return new ContentTypeMatcher(pattern);
    }
}
