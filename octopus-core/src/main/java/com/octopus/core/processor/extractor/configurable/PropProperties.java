package com.octopus.core.processor.extractor.configurable;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * 属性配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class PropProperties implements Validator {

    /**
     * 属性名
     * <p>
     * 默认 空
     */
    private String name;

    /**
     * 属性值
     * <p>
     * 默认 空
     */
    private String value;

    /**
     * 属性值来源提取器的字段名
     * <p>
     * 默认 空
     */
    private String field;

    @Override
    public void validate() throws ValidateException {
        if (StrUtil.isBlank(name)) {
            throw new ValidateException("prop name is required");
        }
    }
}
