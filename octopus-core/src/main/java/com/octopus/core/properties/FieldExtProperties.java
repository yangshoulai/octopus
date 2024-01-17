package com.octopus.core.properties;

import cn.hutool.core.util.CharsetUtil;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validatable;
import lombok.Data;

/**
 * 字段扩展配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class FieldExtProperties implements Validatable {

    /**
     * 是否忽略转换异常
     * <p>
     * 默认 true
     */
    private boolean ignoreError = true;

    /**
     * 布尔类型 定义的false值
     * <p>
     * 默认 （"", "0", "非", "否", "off", "no", "f", "false"）
     */
    private String[] booleanFalseValues = new String[]{"", "0", "非", "否", "off", "no", "f", "false"};

    /**
     * 日期类型 格式
     * <p>
     * 默认 yyyy-MM-dd HH:mm:ss
     */
    private String dateFormatPattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期类型 时区
     * <p>
     * 默认 空
     */
    private String dateFormatTimeZone;

    /**
     * 字节数组类型 编码
     * <p>
     * 默认 UTF-8
     */
    private String charset = CharsetUtil.UTF_8;

    @Override
    public void validate() throws ValidateException {

    }
}
