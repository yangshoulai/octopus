package com.octopus.core.configurable;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.extractor.annotation.Selector;
import com.octopus.core.utils.Validatable;
import lombok.Data;

/**
 * 选择器配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class SelectorProperties implements Validatable {

    /**
     * 类型
     * <p>
     * 默认 None
     */
    private Selector.Type type = Selector.Type.None;

    /**
     * 表达式
     * <p>
     * 默认 空
     */
    private String value;

    /**
     * 默认值
     * <p>
     * 默认 空
     */
    private String def;

    /**
     * Css 选择器 节点属性名称
     * <p>
     * 默认 空
     */
    private String attr;

    /**
     * Css选择器 是否选择节点本身
     * <p>
     * 默认 false
     */
    private boolean self = false;

    /**
     * 正则选择器 分组
     * <p>
     * 默认 0
     */
    private int[] groups = new int[]{0};

    /**
     * 正则选择器 格式化
     * <p>
     * 默认 %s
     */
    private String format = "%s";

    /**
     * Xpath选择器 是否选取节点本身
     * <p>
     * 默认 true
     */
    private boolean node = true;

    /**
     * 格式化配置
     * <p>
     * 默认 默认格式化配置
     */
    private FormatterProperties formatter = new FormatterProperties();

    public SelectorProperties() {
    }

    public SelectorProperties(Selector.Type type) {
        this.type = type;
    }

    public SelectorProperties(Selector.Type type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public void validate() throws ValidateException {
        if (type == null) {
            throw new ValidateException("selector type is required");
        }
        if (formatter != null) {
            formatter.validate();
        }
    }
}
