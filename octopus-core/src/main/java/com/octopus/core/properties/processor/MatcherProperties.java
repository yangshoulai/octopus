package com.octopus.core.properties.processor;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.matcher.Matcher;
import com.octopus.core.processor.matcher.Matchers;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
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
public class MatcherProperties implements Validatable, Transformable<Matcher> {

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
    private String regex;

    /**
     * 响应头匹配器 - 头名称
     * <p>
     * 默认 空
     */
    private String header;

    /**
     * 属性匹配器 - 属性名称
     * <p>
     * 默认 空
     */
    private String attr;

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

    public MatcherProperties(MatcherType type, String regex) {
        this.type = type;
        this.regex = regex;
    }


    @Override
    public void validate() throws ValidateException {
        Validator.notEmpty(type, "matcher type is required");
        if ((type == MatcherType.And || type == MatcherType.Or || type == MatcherType.Not)) {
            Validator.notEmpty(children, "matcher children is required");
        }
        if (type == MatcherType.Not) {
            Validator.eq(children.size(), 1, "not matcher must has only one child");
        }
        if (type == MatcherType.HeaderRegex) {
            Validator.notBlank(header, "header regex matcher header is required");
        }
        if (type == MatcherType.AttrRegex) {
            Validator.notBlank(attr, "attr regex matcher attr is required");
        }
        if (type == MatcherType.ContentTypeRegex || type == MatcherType.HeaderRegex || type == MatcherType.UrlRegex || type == MatcherType.AttrRegex) {
            Validator.notBlank(regex, "regex matcher pattern is required");
        }
    }

    @Override
    public Matcher transform() {
        Matcher[] matchers = {};
        if (children != null) {
            matchers = this.children.stream().map(MatcherProperties::transform).toArray(Matcher[]::new);
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
                matcher = Matchers.headerRegex(header, regex);
                break;
            case ContentTypeRegex:
                matcher = Matchers.contentTypeRegex(regex);
                break;
            case AttrRegex:
                matcher = Matchers.attrRegex(attr, regex);
                break;
            case UrlRegex:
                matcher = Matchers.urlRegex(regex);
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
            case Not:
                matcher = Matchers.not(matchers[0]);
                break;
            default:
                matcher = Matchers.ALL;
                break;
        }
        return matcher;
    }
}
