package com.octopus.core.properties;

import cn.hutool.setting.yaml.YamlUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.impl.ConfigurableProcessor;
import com.octopus.core.processor.Processor;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 处理器配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class ProcessorProperties implements Validatable, Transformable<Processor> {

    /**
     * 匹配器
     * <p>
     * 默认 空
     */
    private MatcherProperties matcher;

    /**
     * 提取器
     * <p>
     * 默认 空
     */
    private ExtractorProperties extractor;

    /**
     * 搜集器
     * <p>
     * 默认 空
     */
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


    @Override
    public void validate() throws ValidateException {
        Validator.notEmpty(matcher, "processor matcher is required");
        Validator.validateWhenNotNull(extractor);
        Validator.validateWhenNotNull(collector);
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

    @Override
    public Processor transform() {
        this.validate();
        return new ConfigurableProcessor(matcher.transform(), extractor, collector == null ? null : collector.transform());
    }
}
