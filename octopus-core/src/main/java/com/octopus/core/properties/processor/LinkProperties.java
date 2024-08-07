package com.octopus.core.properties.processor;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Request;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.properties.PropProperties;
import com.octopus.core.properties.selector.SelectorProperties;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 链接配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class LinkProperties implements Validatable {

    /**
     * 固定链接
     * <p>
     * 默认 空
     */
    private String url = null;

    /**
     * 选择器
     * <p>
     * 默认 空
     */
    private SelectorProperties selector;

    /**
     * 优先级
     * <p>
     * 默认 0
     */
    private int priority = 0;

    /**
     * 是否重复
     * <p>
     * 默认 true
     */
    private boolean repeatable = true;

    /**
     * 是否继承父类配置
     * <p>
     * 默认 false
     */
    private boolean inherit = false;

    /**
     * 是否缓存
     * <p>
     * 默认 false
     */
    private boolean cache = false;

    /**
     * 请求方法
     * <p>
     * 默认 GET
     */
    private Request.RequestMethod method = Request.RequestMethod.GET;

    /**
     * 请求参数配置
     * <p>
     * 默认 空
     */
    private List<PropProperties> params = new ArrayList<>();

    /**
     * 请求体
     * <p>
     * 默认 空
     */
    private String body;

    /**
     * 请求头配置
     * <p>
     * 默认 空
     */
    private List<PropProperties> headers = new ArrayList<>();

    /**
     * 请求属性配置
     * <p>
     * 默认 空
     */
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
        Validator.validateWhenNotNull(selector);
        Validator.validateWhenNotNull(params);
        Validator.validateWhenNotNull(headers);
        Validator.validateWhenNotNull(attrs);
    }
}
