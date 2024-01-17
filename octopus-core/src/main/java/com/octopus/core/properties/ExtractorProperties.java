package com.octopus.core.properties;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validatable;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 提取器配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class ExtractorProperties implements Validatable {

    /**
     * 链接列表
     * <p>
     * 默认 空
     */
    private List<LinkProperties> links = new ArrayList<>();

    /**
     * 内容字段列表
     * <p>
     * 默认 空
     */
    private List<FieldProperties> fields = new ArrayList<>();

    public ExtractorProperties() {
    }

    public ExtractorProperties(List<LinkProperties> links, List<FieldProperties> fields) {
        this.links = links;
        this.fields = fields;
    }

    @Override
    public void validate() throws ValidateException {
        if (links != null) {
            for (LinkProperties link : links) {
                link.validate();
            }
        }
        if (fields != null) {
            for (FieldProperties field : fields) {
                field.validate();
            }
        }
    }
}
