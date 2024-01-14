package com.octopus.core.processor.extractor.configurable;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * 字段配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class FieldProperties implements Validator {

    /**
     * 字段名
     * 默认 空
     */
    private String name;

    /**
     * 字段类型
     * <p>
     * 默认 String
     */
    private FieldType type = FieldType.String;

    /**
     * 是否多选
     * <p>
     * 默认 false
     */
    private boolean multi = false;

    /**
     * 选择器
     * <p>
     * 默认 空
     */
    private SelectorProperties selector;

    /**
     * 提取器
     * <p>
     * 默认 空
     */
    private ExtractorProperties extractor;

    /**
     * 扩展配置
     * <p>
     * 默认 空
     */
    private FieldExtProperties ext;

    public FieldProperties() {
    }

    public FieldProperties(String name, SelectorProperties selector) {
        this.name = name;
        this.selector = selector;
    }


    public FieldProperties(String name, SelectorProperties selector, boolean multi) {
        this.name = name;
        this.multi = multi;
        this.selector = selector;
    }

    public FieldProperties(String name, SelectorProperties selector, FieldType type) {
        this.name = name;
        this.type = type;
        this.selector = selector;
    }


    public FieldProperties(String name, SelectorProperties selector, FieldType type, boolean multi) {
        this.name = name;
        this.type = type;
        this.multi = multi;
        this.selector = selector;
    }

    public FieldProperties(String name, SelectorProperties selector, ExtractorProperties extractor) {
        this.name = name;
        this.selector = selector;
        this.extractor = extractor;
    }

    public FieldProperties(String name, SelectorProperties selector, ExtractorProperties extractor, boolean multi) {
        this.name = name;
        this.selector = selector;
        this.extractor = extractor;
        this.multi = multi;
    }

    @Override
    public void validate() throws ValidateException {
        if (this.extractor == null && this.type == null) {
            throw new ValidateException("field type or extractor is required");
        }
        if (this.extractor != null) {
            this.extractor.validate();
        }
        if (selector == null) {
            throw new ValidateException("field selector is required");
        }
        this.selector.validate();
        if (StrUtil.isBlank(name)) {
            throw new ValidateException("field name is required");
        }
        if (this.ext != null) {
            this.ext.validate();
        }
    }
}
