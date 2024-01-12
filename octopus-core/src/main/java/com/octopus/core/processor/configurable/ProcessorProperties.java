package com.octopus.core.processor.configurable;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class ProcessorProperties implements Validator {

    private MatcherProperties matcher;

    private ExtractorProperties extractor;

    public ProcessorProperties() {

    }

    public ProcessorProperties(ExtractorProperties extractor) {
        this(new MatcherProperties(MatcherType.All), extractor);
    }

    public ProcessorProperties(MatcherProperties matcher, ExtractorProperties extractor) {
        this.matcher = matcher;
        this.extractor = extractor;
    }

    @Override
    public void validate() throws ValidateException {
        if (matcher == null) {
            throw new ValidateException("processor matcher is required");
        }
        if (extractor == null) {
            throw new ValidateException("processor extractor is required");
        }
        extractor.validate();
    }
}
