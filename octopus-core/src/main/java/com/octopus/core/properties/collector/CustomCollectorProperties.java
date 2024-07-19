package com.octopus.core.properties.collector;

import cn.hutool.core.util.ClassUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.collector.AbstractCustomCollector;
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
public class CustomCollectorProperties implements Validatable {

    private String collector;

    private Properties conf;

    public CustomCollectorProperties() {
    }

    public CustomCollectorProperties(String collector) {
        this.collector = collector;
    }

    @Override
    public void validate() throws ValidateException {
        Validator.notBlank(collector, "custom collector class is required");
        try {
            Class<?> cls = ClassUtil.loadClass(collector);
            if (!AbstractCustomCollector.class.isAssignableFrom(cls)) {
                throw new ValidateException("class [" + collector + "] must extends AbstractCustomCollector");
            }
            if (ClassUtil.isAbstract(cls)) {
                throw new ValidateException("class [" + collector + "] must not be abstract");
            }
        } catch (Exception e) {
            throw new ValidateException("can not load collector [" + collector + "]");
        }
    }
}
