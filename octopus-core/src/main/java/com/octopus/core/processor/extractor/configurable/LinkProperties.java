package com.octopus.core.processor.extractor.configurable;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class LinkProperties implements Validator {

    private String url = "";

    private SelectorProperties selector = new SelectorProperties();

    private int priority = 0;

    private boolean repeatable = true;

    private boolean inherit = false;

    private Request.RequestMethod method = Request.RequestMethod.GET;

    private List<PropProperties> params = new ArrayList<>();

    private List<PropProperties> headers = new ArrayList<>();

    private List<PropProperties> attrs = new ArrayList<>();

    public LinkProperties() {
    }

    public LinkProperties(SelectorProperties selector) {
        this.selector = selector;
    }

    @Override
    public void validate() throws ValidateException {
        if (StrUtil.isBlank(url) && selector == null) {
            throw new ValidateException("link url or selector is required");
        }
        if (selector != null) {
            selector.validate();
        }
        if (params != null) {
            for (PropProperties prop : params) {
                prop.validate();
            }
        }
        if (headers != null) {
            for (PropProperties prop : headers) {
                prop.validate();
            }
        }
        if (attrs != null) {
            for (PropProperties prop : attrs) {
                prop.validate();
            }
        }
    }
}
