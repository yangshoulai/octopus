package com.octopus.core.processor.extractor.configurable;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * 格式化配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class FormatterProperties implements Validator {

    /**
     * 是否去除空格
     * <p>
     * 默认 true
     */
    private boolean trim = true;

    /**
     * 是否过滤空白行
     * <p>
     * 默认 true
     */
    private boolean filter = true;


    /**
     * 是否分割
     * <p>
     * 默认 false
     */
    private boolean split = false;

    /**
     * 分隔符
     * <p>
     * 默认 "；|;|，|,|#| |、|/|\\\\|\\|"
     */
    private String separator = "；|;|，|,|#| |、|/|\\\\|\\|";


    /**
     * 二次提取 - 正则表达式
     * <p>
     * 默认 空（不提取）
     */
    private String regex = "";


    /**
     * 二次提取 - 格式化
     * <p>
     * 默认 %s
     */
    private String format = "%s";

    /**
     * 二次提取 - 选取的正则表达式分组
     * <p>
     * 默认 {0}
     */
    private int[] groups = {0};

    @Override
    public void validate() throws ValidateException {

    }
}
