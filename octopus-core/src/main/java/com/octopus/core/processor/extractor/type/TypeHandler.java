package com.octopus.core.processor.extractor.type;

import com.octopus.core.processor.extractor.FieldExt;

/**
 * 类型转换器
 *
 * <p>将选中的字符转换成特定类型
 *
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public interface TypeHandler<T> {

    /**
     * 转换
     *
     * @param source 元字符
     * @param ext    转换器额外注解
     * @return 转换后的对象
     */
    T handle(String source, FieldExt ext);

}
