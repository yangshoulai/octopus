package com.octopus.core.properties.processor;

import cn.hutool.core.util.ClassUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.impl.AbstractCustomProcessor;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.Properties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/19
 */
@Data
public class CustomerProcessorProperties implements Validatable {

    private String processor;

    private Properties conf;

    public CustomerProcessorProperties() {
    }

    public CustomerProcessorProperties(String processor) {
        this.processor = processor;
    }

    @Override
    public void validate() throws ValidateException {
        Validator.notBlank(processor, "custom processor class is required");
        try {
            Class<?> cls = ClassUtil.loadClass(processor);
            if (!AbstractCustomProcessor.class.isAssignableFrom(cls)) {
                throw new ValidateException("class [" + processor + "] must extends AbstractCustomProcessor");
            }
            if (ClassUtil.isAbstract(cls)) {
                throw new ValidateException("class [" + processor + "] must not be abstract");
            }
        } catch (Exception e) {
            throw new ValidateException("can not load custom processor [" + processor + "]");
        }

    }
}
