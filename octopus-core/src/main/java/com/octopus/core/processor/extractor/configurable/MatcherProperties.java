package com.octopus.core.processor.extractor.configurable;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.matcher.Matcher;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 匹配器配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class MatcherProperties implements Validator {

    /**
     * 匹配器类型
     * <p>
     * 默认 空
     */
    private MatcherType type;

    /**
     * 正则匹配器 - 正则表达式
     * <p>
     * 默认 空
     */
    private String pattern;

    /**
     * 响应头匹配器 - 头名称
     * <p>
     * 默认 空
     */
    private String header;

    /**
     * 组合匹配器 子匹配器列表
     * <p>
     * 默认 空
     */
    private List<MatcherProperties> children = new ArrayList<>();

    public MatcherProperties() {
    }

    public MatcherProperties(MatcherType type) {
        this.type = type;
    }

    public MatcherProperties(MatcherType type, String pattern) {
        this.type = type;
        this.pattern = pattern;
    }

    public Matcher toMatcher() {
        Matcher[] matchers = {};
        if (children != null) {
            matchers = this.children.stream().map(MatcherProperties::toMatcher).toArray(Matcher[]::new);
        }
        Matcher matcher = null;
        switch (type) {
            case Or:
                matcher = Matchers.or(matchers);
                break;
            case And:
                matcher = Matchers.and(matchers);
                break;
            case All:
                matcher = Matchers.ALL;
                break;
            case HeaderRegex:
                matcher = Matchers.headerRegex(header, pattern);
                break;
            case ContentTypeRegex:
                matcher = Matchers.contentTypeRegex(pattern);
                break;
            case UrlRegex:
                matcher = Matchers.urlRegex(pattern);
                break;
            case Json:
                matcher = Matchers.JSON;
                break;
            case Image:
                matcher = Matchers.IMAGE;
                break;
            case Video:
                matcher = Matchers.VIDEO;
                break;
            case Pdf:
                matcher = Matchers.PDF;
                break;
            case Word:
                matcher = Matchers.WORD;
                break;
            case Excel:
                matcher = Matchers.EXCEL;
                break;
            case Audio:
                matcher = Matchers.AUDIO;
                break;
            case OctetStream:
                matcher = Matchers.OCTET_STREAM;
                break;
            case Html:
                matcher = Matchers.HTML;
                break;
            case Media:
                matcher = Matchers.MEDIA;
                break;
            default:
                matcher = Matchers.ALL;
                break;
        }
        return matcher;
    }

    @Override
    public void validate() throws ValidateException {
        if (type == null) {
            throw new ValidateException("matcher type is required");
        }
        if ((type == MatcherType.And || type == MatcherType.Or) && (this.children == null || this.children.isEmpty())) {
            throw new ValidateException("matcher children is required");
        }
    }
}
