package com.octopus.core.processor.configurable.selector;

import com.octopus.core.Response;
import com.octopus.core.processor.configurable.SelectorProperties;
import com.octopus.core.processor.extractor.selector.SelectException;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public interface FieldSelector {

    List<String> select(String source, boolean multi, SelectorProperties selector, Response response) throws SelectException;
}
