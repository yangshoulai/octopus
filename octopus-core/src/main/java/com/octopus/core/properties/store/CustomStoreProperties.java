package com.octopus.core.properties.store;

import cn.hutool.core.util.ClassUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.store.AbstractCustomStore;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.util.Properties;

/**
 * 请求存储器配置
 * <p>
 * 默认使用内存
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class CustomStoreProperties implements Validatable {

    private String store;

    private Properties conf;

    public CustomStoreProperties() {
    }

    public CustomStoreProperties(String store) {
        this.store = store;
    }

    @Override
    public void validate() throws ValidateException {
        Validator.notBlank(store, "custom store class is required");
        try {
            Class<?> cls = ClassUtil.loadClass(store);
            if (!AbstractCustomStore.class.isAssignableFrom(cls)) {
                throw new ValidateException("class [" + store + "] must extends AbstractCustomStore");
            }
            if (ClassUtil.isAbstract(cls)) {
                throw new ValidateException("class [" + store + "] must not be abstract");
            }
        } catch (Exception e) {
            throw new ValidateException("can not load store [" + store + "]");
        }
    }

}
