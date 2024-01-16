package com.octopus.core.processor;

import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;
import com.octopus.core.exception.SelectException;

import java.util.List;

/**
 * 选择器
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public interface Selector {

    /**
     * 基于源内容选择匹配的内容
     *
     * @param source   源内容
     * @param multi    是否多选
     * @param selector 配置
     * @param response 响应
     * @return 选中的内容集合
     * @throws SelectException 异常
     */
    List<String> select(String source, boolean multi, SelectorProperties selector, Response response) throws SelectException;
}
