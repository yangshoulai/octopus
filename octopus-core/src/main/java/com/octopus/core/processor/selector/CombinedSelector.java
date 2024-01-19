package com.octopus.core.processor.selector;

import com.octopus.core.Response;
import com.octopus.core.processor.Selector;
import com.octopus.core.properties.selector.AbstractSelectorProperties;
import com.octopus.core.properties.selector.SelectorProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/1/18
 */
public class CombinedSelector extends AbstractSelector<SelectorProperties> {

    private static final AttrSelector ATTR = new AttrSelector();

    private static final BodySelector BODY = new BodySelector();

    private static final CssSelector CSS = new CssSelector();

    private static final HeaderSelector HEADER = new HeaderSelector();

    private static final JsonSelector JSON = new JsonSelector();

    private static final NoneSelector NONE = new NoneSelector();

    private static final ParamSelector PARAM = new ParamSelector();

    private static final RegexSelector REGEX = new RegexSelector();

    private static final UrlSelector URL = new UrlSelector();

    private static final ValueSelector VALUE = new ValueSelector();

    private static final XpathSelector XPATH = new XpathSelector();

    private static final IdSelector ID = new IdSelector();

    private static final EnvSelector ENV = new EnvSelector();


    @Override
    protected List<String> doMultiSelect(String source, SelectorProperties selector, Response response) {
        List<String> selected = new ArrayList<>();
        delegateSelect(ATTR, selector.getAttr(), source, response, selected);
        delegateSelect(BODY, selector.getBody(), source, response, selected);
        delegateSelect(CSS, selector.getCss(), source, response, selected);
        delegateSelect(HEADER, selector.getHeader(), source, response, selected);
        delegateSelect(JSON, selector.getJson(), source, response, selected);
        delegateSelect(NONE, selector.getNone(), source, response, selected);
        delegateSelect(PARAM, selector.getParam(), source, response, selected);
        delegateSelect(REGEX, selector.getRegex(), source, response, selected);
        delegateSelect(URL, selector.getUrl(), source, response, selected);
        delegateSelect(VALUE, selector.getValue(), source, response, selected);
        delegateSelect(XPATH, selector.getXpath(), source, response, selected);
        delegateSelect(ID, selector.getId(), source, response, selected);
        delegateSelect(ENV, selector.getEnv(), source, response, selected);
        return selected;
    }

    private <A extends AbstractSelectorProperties> void delegateSelect(Selector<A> selector, A properties, String source, Response response, List<String> container) {
        if (selector != null && properties != null) {
            List<String> selected = selector.select(source, true, properties, response);
            if (selected != null) {
                container.addAll(selected);
            }
        }
    }
}
