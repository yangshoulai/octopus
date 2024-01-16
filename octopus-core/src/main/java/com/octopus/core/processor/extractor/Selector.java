package com.octopus.core.processor.extractor;

import com.octopus.core.Response;
import com.octopus.core.configurable.SelectorProperties;
import com.octopus.core.exception.SelectException;

import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public interface Selector {

    List<String> select(String source, boolean multi, SelectorProperties selector, Response response) throws SelectException;
}
