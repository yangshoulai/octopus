package com.octopus.core.processor.collector;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import com.octopus.core.Response;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.processor.Collector;
import com.octopus.core.properties.collector.MongodbCollectorProperties;
import org.bson.Document;

import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/07/19
 */
public class MongodbCollector<R> implements Collector<R> {

    private final MongodbCollectorProperties properties;

    private final MongoCollection<Document> collection;

    public MongodbCollector(MongodbCollectorProperties properties) {
        this.properties = properties;
        MongoClient client = MongoClients.create(properties.getUrl());
        this.collection = client.getDatabase(properties.getDatabase()).getCollection(properties.getCollection());
    }

    @Override
    public void collect(R result, Response response) {
        Document document = Document.parse(JSONUtil.toJsonStr(result));
        String id = document.get(properties.getIdFieldName(), String.class);
        if (StrUtil.isBlank(id)) {
            throw new OctopusException("id not found on result [" + result.getClass().getName() + "]");
        }
        document.put("_id", id);
        this.collection.replaceOne(
                Filters.eq("_id", id), document, new ReplaceOptions().upsert(true));
    }
}
