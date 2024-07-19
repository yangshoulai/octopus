package com.octopus.core.properties.processor;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.setting.yaml.YamlUtil;
import com.octopus.core.OctopusBuilder;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.logging.Logger;
import com.octopus.core.logging.LoggerFactory;
import com.octopus.core.processor.Processor;
import com.octopus.core.processor.impl.AbstractCustomProcessor;
import com.octopus.core.processor.impl.ConfigurableProcessor;
import com.octopus.core.properties.collector.CollectorProperties;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 处理器配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class ProcessorProperties implements Validatable, Transformable<Processor> {
    private Logger logger = LoggerFactory.getLogger(OctopusBuilder.class);
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
     * 自定义处理器
     */
    private CustomerProcessorProperties custom;

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
        Validator.validateWhenNotNull(custom);
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
        if (custom != null) {
            logger.info("Custom processor " + custom.getProcessor() + " found, ignore other extractor conf");
            Class<? extends AbstractCustomProcessor> cls = ClassUtil.loadClass(custom.getProcessor());
            try {
                return ReflectUtil.getConstructor(cls, Properties.class).newInstance(custom.getConf());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ConfigurableProcessor(matcher.transform(), extractor, collector == null ? null : collector.transform());
        }

    }
}
