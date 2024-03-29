package com.octopus.core.store;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import com.octopus.core.Request;
import com.octopus.core.Request.State;
import com.octopus.core.Request.Status;
import com.octopus.core.properties.store.MongoStoreProperties;
import com.octopus.core.replay.ReplayFilter;
import lombok.NonNull;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

/**
 * Mongo 存储器
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/12/8
 */
public class MongoStore implements Store {
    private static final String INDEX_PRIORITY_NAME = "idx_priority";

    private static final String INDEX_STATE_NAME = "idx_status_state";

    private final MongoClient mongoClient;

    private final MongoCollection<Document> requests;


    public MongoStore(@NonNull String uri, @NonNull String database, @NonNull String collection) {
        this(new MongoStoreProperties(uri, database, collection));
    }

    public MongoStore() {
        this(new MongoStoreProperties());
    }


    public MongoStore(@NonNull MongoStoreProperties properties) {
        MongoClientURI u = new MongoClientURI(properties.getUri());
        this.mongoClient = new MongoClient(u);
        this.requests = mongoClient.getDatabase(properties.getDatabase()).getCollection(properties.getCollection(), Document.class);
        this.createIndexesIfNecessary();
        this.updateStatus(Status.of(State.Executing), Status.of(State.Waiting));
    }

    @Override
    public Request get() {
        Document request = null;
        ClientSession session = this.mongoClient.startSession();
        try {
            session.startTransaction();
            request =
                    this.requests
                            .find()
                            .filter(Filters.eq("status.state", State.Waiting.name()))
                            .sort(Sorts.descending("priority"))
                            .limit(1)
                            .first();
            if (request != null) {
                this.updateStatusById(request.get("_id"), Status.of(State.Executing));
            }
            session.commitTransaction();
        } catch (Exception e) {
            session.abortTransaction();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return request == null
                ? null
                : JSONUtil.toBean(request.toJson(), Request.class).setStatus(Status.of(State.Executing));
    }

    @Override
    public boolean put(Request request) {
        Document document = Document.parse(JSONUtil.toJsonStr(request));
        String id = request.getId();
        document.put("_id", id);
        UpdateResult result =
                this.requests.replaceOne(
                        Filters.eq("_id", id), document, new ReplaceOptions().upsert(true));
        return result.wasAcknowledged();
    }

    @Override
    public boolean exists(Request request) {
        return this.requests.countDocuments(Filters.eq("_id", request.getId())) > 0;
    }

    @Override
    public void clear() {
        this.requests.deleteMany(new Document());
    }

    @Override
    public void markAsCompleted(Request request) {
        this.updateStatusById(request.getId(), Status.of(State.Completed));
    }

    @Override
    public void markAsFailed(Request request, String error) {
        this.updateStatusById(request, Status.of(State.Failed, error));
    }

    @Override
    public long getTotalSize() {
        return this.requests.countDocuments();
    }

    @Override
    public long getCompletedSize() {
        return this.requests.countDocuments(Filters.eq("status.state", State.Completed.name()));
    }

    @Override
    public long getWaitingSize() {
        return this.requests.countDocuments(Filters.eq("status.state", State.Waiting.name()));
    }

    @Override
    public long getFailedSize() {
        return this.requests.countDocuments(Filters.eq("status.state", State.Failed.name()));
    }


    @Override
    public void delete(String id) {
        this.requests.deleteOne(Filters.eq("_id", id));
    }

    @Override
    public int replayFailed(ReplayFilter filter) {
        List<String> fails = new ArrayList<>();
        int skipSize = 0;
        int batchSize = 1000;
        boolean hasMore;
        do {
            int batchFailedSize = 0;
            MongoIterable<Request> iterable =
                    this.requests
                            .find()
                            .filter(Filters.eq("status.state", State.Failed.name()))
                            .skip(skipSize)
                            .limit(batchSize)
                            .map(d -> JSONUtil.toBean(d.toJson(), Request.class));
            try (MongoCursor<Request> cursor = iterable.cursor()) {
                while (cursor.hasNext()) {
                    batchFailedSize++;
                    Request request = cursor.next();
                    if (filter.filter(request)) {
                        fails.add(request.getId());
                    }
                }
            }
            hasMore = batchFailedSize >= batchSize;
            if (hasMore) {
                skipSize += batchSize;
            }
        } while (hasMore);

        for (String e : fails) {
            this.updateStatusById(e, Status.of(State.Waiting));
        }
        return fails.size();
    }

    private void updateStatusById(Object id, Status status) {
        List<Bson> updates = ListUtil.toList(
                Updates.set("status.state", status.getState().name()),
                Updates.set("status.message", status.getMessage()));
        if (status.getState() == State.Failed && id instanceof Request) {
            updates.add(Updates.set("failTimes", ((Request) id).getFailTimes() + 1));
        }
        Bson filter = Filters.eq("_id", id instanceof Request ? ((Request) id).getId() : id);
        this.requests.updateOne(filter, updates);
    }

    private void updateStatus(Status oldStatus, Status newStatus) {
        this.requests.updateMany(
                Filters.eq("status.state", oldStatus.getState().name()),
                ListUtil.toList(
                        Updates.set("status.state", newStatus.getState().name()),
                        Updates.set("status.message", newStatus.getMessage())));
    }

    private void createIndexesIfNecessary() {
        List<String> indexNames = new ArrayList<>();
        for (Document index : this.requests.listIndexes()) {
            indexNames.add(index.getString("name"));
        }
        if (!indexNames.contains(INDEX_PRIORITY_NAME)) {
            requests.createIndex(Indexes.ascending("priority"), new IndexOptions().name(INDEX_PRIORITY_NAME));
        }
        if (!indexNames.contains(INDEX_STATE_NAME)) {
            requests.createIndex(
                    Indexes.ascending("status.state"), new IndexOptions().name(INDEX_STATE_NAME));
        }
    }
}
