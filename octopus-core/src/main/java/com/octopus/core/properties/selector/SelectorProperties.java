package com.octopus.core.properties.selector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.properties.selector.*;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SelectorProperties extends AbstractSelectorProperties {

    private AttrSelectorProperties attr;

    private BodySelectorProperties body;

    private CssSelectorProperties css;

    private HeaderSelectorProperties header;

    private JsonSelectorProperties json;

    private ParamSelectorProperties param;

    private RegexSelectorProperties regex;

    private UrlSelectorProperties url;

    private ValueSelectorProperties value;

    private XpathSelectorProperties xpath;

    private IdSelectorProperties id;

    private EnvSelectorProperties env;

    private NoneSelectorProperties none = new NoneSelectorProperties();

    @Override
    public void validate() throws ValidateException {
        super.validate();
        if (attr == null && body == null && css == null && header == null && json == null && param == null && regex == null
                && url == null && value == null && xpath == null && none != null) {
            throw new ValidateException("at least one selector is required");
        }
        Validator.validateWhenNotNull(attr);
        Validator.validateWhenNotNull(body);
        Validator.validateWhenNotNull(css);
        Validator.validateWhenNotNull(header);
        Validator.validateWhenNotNull(json);
        Validator.validateWhenNotNull(param);
        Validator.validateWhenNotNull(regex);
        Validator.validateWhenNotNull(url);
        Validator.validateWhenNotNull(value);
        Validator.validateWhenNotNull(xpath);
        Validator.validateWhenNotNull(none);
    }
}
