package com.octopus.core.processor.extractor.configurable;

import cn.hutool.setting.yaml.YamlUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.ConfigurableProcessor;
import com.octopus.core.processor.Processor;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class ProcessorProperties implements Validator {

    private MatcherProperties matcher;

    private ExtractorProperties extractor;

    private CollectorProperties collector;

    public ProcessorProperties() {
        this(null);
    }

    public ProcessorProperties(MatcherProperties matcher) {
        this(matcher, null);
    }

    public ProcessorProperties(MatcherProperties matcher, ExtractorProperties extractor) {
        this(matcher, extractor, null);
    }

    public ProcessorProperties(MatcherProperties matcher, ExtractorProperties extractor, CollectorProperties collector) {
        this.matcher = matcher;
        this.extractor = extractor;
        this.collector = collector;
    }

    public Processor toProcessor() {
        this.validate();
        return new ConfigurableProcessor(matcher.toMatcher(), extractor, collector == null ? null : collector.toCollector());
    }

    @Override
    public void validate() throws ValidateException {
        if (matcher == null) {
            throw new ValidateException("processor matcher is required");
        }
        matcher.validate();
        if (extractor != null) {
            extractor.validate();
        }

        if (collector != null) {
            collector.validate();
        }
    }

    public static ProcessorProperties fromYaml(InputStream inputStream) {
        return YamlUtil.load(inputStream, ProcessorProperties.class);
    }

    public static ProcessorProperties fromYaml(String filePath) {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            return fromYaml(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
