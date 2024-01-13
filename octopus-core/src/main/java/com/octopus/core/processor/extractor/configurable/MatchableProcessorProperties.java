package com.octopus.core.processor.extractor.configurable;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.AbstractDownloadProcessor;
import com.octopus.core.processor.Processor;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.io.File;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/14
 */
@Data
public abstract class MatchableProcessorProperties implements Validator {

    private MatcherProperties matcher;

    public MatchableProcessorProperties() {
    }

    public MatchableProcessorProperties(MatcherProperties matcher) {
        this.matcher = matcher;
    }

    @Override
    public void validate() throws ValidateException {
        if (matcher == null) {
            throw new ValidateException("processor matcher is null");
        }
    }


    public abstract Processor toProcessor();

}
