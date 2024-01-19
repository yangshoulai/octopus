package com.octopus.core.processor;

import com.octopus.core.Response;
import com.octopus.core.processor.selector.CombinedSelector;
import com.octopus.core.properties.selector.SelectorProperties;
import com.octopus.core.properties.selector.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择器注册器
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public class SelectorHelper {

    private static final CombinedSelector SELECTOR = new CombinedSelector();

    private SelectorHelper() {

    }

    public static SelectorHelper getInstance() {
        return SelectorHelper.Holder.INSTANCE;
    }

    private static DenoiserProperties getDenoiserProperties(com.octopus.core.processor.annotation.Selector selector) {
        if (selector.denoiser() != null) {
            DenoiserProperties denoiser = new DenoiserProperties();
            denoiser.setSeparator(selector.denoiser().separator());
            denoiser.setFilter(selector.denoiser().filter());
            denoiser.setTrim(selector.denoiser().trim());
            denoiser.setSplit(selector.denoiser().split());
            denoiser.setRegex(selector.denoiser().regex());
            denoiser.setFormat(selector.denoiser().format());
            denoiser.setGroups(selector.denoiser().groups());
            return denoiser;
        }
        return null;
    }


    public List<String> selectBySelectorProperties(List<SelectorProperties> selectors, String source, boolean multi, Response response) {
        List<String> selected = new ArrayList<>();
        for (SelectorProperties selector : selectors) {
            List<String> items = selectBySelectorProperties(selector, source, multi, response);
            if (items != null) {
                selected.addAll(items);
            }
            if (!selected.isEmpty()) {
                break;
            }
        }
        return selected;
    }

    public List<String> selectBySelectorProperties(SelectorProperties selector, String source, boolean multi, Response response) {
        return SELECTOR.select(source, multi, selector, response);
    }

    public List<String> selectBySelectorAnnotation(List<com.octopus.core.processor.annotation.Selector> selectors, String source, boolean multi, Response response) {
        List<String> selected = new ArrayList<>();
        for (com.octopus.core.processor.annotation.Selector selector : selectors) {
            List<String> items = selectBySelectorAnnotation(selector, source, multi, response);
            if (items != null && !items.isEmpty()) {
                selected.addAll(items);
            }
            if (!selected.isEmpty()) {
                break;
            }
        }
        return selected;
    }

    public List<String> selectBySelectorAnnotation(com.octopus.core.processor.annotation.Selector selector, String source, boolean multi, Response response) {
        SelectorProperties properties = new SelectorProperties();
        DenoiserProperties denoiser = getDenoiserProperties(selector);

        switch (selector.type()) {
            case Attr:
                AttrSelectorProperties attr = new AttrSelectorProperties(selector.value());
                attr.setDefaultValue(selector.def());
                attr.setDenoiser(denoiser);
                properties.setAttr(attr);
                break;
            case Body:
                BodySelectorProperties body = new BodySelectorProperties();
                body.setDefaultValue(selector.def());
                body.setDenoiser(denoiser);
                properties.setBody(body);
                break;
            case Css:
                CssSelectorProperties css = new CssSelectorProperties(selector.value(), selector.self(), selector.attr());
                css.setDefaultValue(selector.def());
                css.setDenoiser(denoiser);
                properties.setCss(css);
                break;
            case Header:
                HeaderSelectorProperties header = new HeaderSelectorProperties(selector.value());
                header.setDefaultValue(selector.def());
                header.setDenoiser(denoiser);
                properties.setHeader(header);
                break;
            case Json:
                JsonSelectorProperties json = new JsonSelectorProperties(selector.value());
                json.setDefaultValue(selector.def());
                json.setDenoiser(denoiser);
                properties.setJson(json);
                break;
            case None:
                NoneSelectorProperties none = new NoneSelectorProperties();
                none.setDefaultValue(selector.def());
                none.setDenoiser(denoiser);
                properties.setNone(none);
                break;
            case Param:
                ParamSelectorProperties param = new ParamSelectorProperties(selector.value());
                param.setDefaultValue(selector.def());
                param.setDenoiser(denoiser);
                properties.setParam(param);
                break;
            case Regex:
                RegexSelectorProperties regex = new RegexSelectorProperties(selector.value(), selector.groups(), selector.format());
                regex.setDefaultValue(selector.def());
                regex.setDenoiser(denoiser);
                properties.setRegex(regex);
                break;
            case Url:
                UrlSelectorProperties url = new UrlSelectorProperties();
                url.setDefaultValue(selector.def());
                url.setDenoiser(denoiser);
                properties.setUrl(url);
                break;
            case Value:
                ValueSelectorProperties value = new ValueSelectorProperties(selector.value());
                value.setDefaultValue(selector.def());
                value.setDenoiser(denoiser);
                properties.setValue(value);
                break;
            case Xpath:
                XpathSelectorProperties xpath = new XpathSelectorProperties(selector.value(), selector.node());
                xpath.setDefaultValue(selector.def());
                xpath.setDenoiser(denoiser);
                properties.setXpath(xpath);
                break;
            case Id:
                IdSelectorProperties id = new IdSelectorProperties();
                id.setDefaultValue(selector.def());
                id.setDenoiser(denoiser);
                properties.setId(id);
                break;
            case Env:
                EnvSelectorProperties env = new EnvSelectorProperties(selector.value());
                env.setDefaultValue(selector.def());
                env.setDenoiser(denoiser);
                properties.setEnv(env);
                break;
            default:
                break;
        }
        return selectBySelectorProperties(properties, source, multi, response);
    }

    private static class Holder {

        public static final SelectorHelper INSTANCE = new SelectorHelper();
    }
}
