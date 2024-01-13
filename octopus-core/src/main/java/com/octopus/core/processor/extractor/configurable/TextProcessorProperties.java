package com.octopus.core.processor.extractor.configurable;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.yaml.YamlUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.AbstractDownloadProcessor;
import com.octopus.core.processor.ConfigurableProcessor;
import com.octopus.core.processor.Processor;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TextProcessorProperties extends MatchableProcessorProperties {

    private ExtractorProperties extractor;

    public TextProcessorProperties() {

    }

    public TextProcessorProperties(ExtractorProperties extractor) {
        this(new MatcherProperties(MatcherType.All), extractor);
    }

    public TextProcessorProperties(MatcherProperties matcher, ExtractorProperties extractor) {
        super(matcher);
        this.extractor = extractor;
    }

    @Override
    public ConfigurableProcessor toProcessor() {
        this.validate();
        return new ConfigurableProcessor(this);
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        if (extractor == null) {
            throw new ValidateException("processor extractor is required");
        }
        extractor.validate();
    }

    public static TextProcessorProperties fromYaml(InputStream inputStream) {
        return YamlUtil.load(inputStream, TextProcessorProperties.class);
    }

    public static TextProcessorProperties fromYaml(String filePath) {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            return fromYaml(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
