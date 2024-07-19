package com.octopus.core.properties.collector;

import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/07/19
 */
@Data
public class MongodbCollectorProperties implements Validatable {

    private String url;

    private String database;

    private String collection;

    private String idFieldName;

    @Override
    public void validate() throws ValidateException {
        Validator.notEmpty(this.url, "mongodb url is required");
        Validator.notEmpty(this.database, "mongodb database is required");
        Validator.notEmpty(this.collection, "mongodb collection is required");
        Validator.notEmpty(this.idFieldName, "mongodb idFieldName is required");
    }
}
