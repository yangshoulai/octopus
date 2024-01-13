package com.octopus.core.processor.extractor.configurable;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.yaml.YamlUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.AbstractDownloadProcessor;
import com.octopus.core.processor.Processor;
import com.octopus.core.utils.Validator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 下载文件
 * 文件保存为 {dir}/{attr[fileDirProp]}/{attr[fileNameProp]}
 * @author shoulai.yang@gmail.com
 * @date 2024/01/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BinaryProcessorProperties extends MatchableProcessorProperties {

    public static final String DEFAULT_FILE_DIR_PROP = "_file_dir";

    public static final String DEFAULT_FILE_NAME_PROP = "_file_name";

    /**
     * 基础文件下载目录
     */

    private String dir;

    /**
     * 下载文件夹，从该请求属性中获取
     */
    private String fileDirProp = DEFAULT_FILE_DIR_PROP;

    /**
     * 下载文件名，从该请求属性中获取
     */
    private String fileNameProp = DEFAULT_FILE_NAME_PROP;

    public BinaryProcessorProperties() {
    }

    public BinaryProcessorProperties(MatcherProperties matcher) {
        super(matcher);
    }

    @Override
    public void validate() throws ValidateException {
        super.validate();
        if (StrUtil.isBlank(dir)) {
            throw new ValidateException("downloader dir is required");
        }
    }

    @Override
    public Processor toProcessor() {
        this.validate();

        return new AbstractDownloadProcessor(getMatcher().toMatcher()) {
            @Override
            protected File resolveSaveDir(Response response) {
                Object fileDir = response.getRequest().getAttribute(fileDirProp);
                if (fileDir != null && StrUtil.isNotBlank(fileDir.toString())) {
                    return new File(dir, fileDir.toString());
                }
                return new File(dir);
            }

            @Override
            protected String resolveSaveName(Response response) {
                Object fileName = response.getRequest().getAttribute(fileNameProp);
                if (fileName != null && StrUtil.isNotBlank(fileName.toString())) {
                    return fileName.toString();
                }
                return super.resolveSaveName(response);
            }
        };
    }

    public static BinaryProcessorProperties fromYaml(InputStream inputStream) {
        return YamlUtil.load(inputStream, BinaryProcessorProperties.class);
    }

    public static BinaryProcessorProperties fromYaml(String filePath) {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            return fromYaml(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
