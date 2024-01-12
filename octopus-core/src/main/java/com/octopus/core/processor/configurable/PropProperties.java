package com.octopus.core.processor.configurable;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class PropProperties implements Validator {

    private String name = "";

    private String value = "";

    private String field = "";

    @Override
    public void validate() throws ValidateException {
        if (StrUtil.isBlank(name)) {
            throw new ValidateException("prop name is required");
        }
    }
}
