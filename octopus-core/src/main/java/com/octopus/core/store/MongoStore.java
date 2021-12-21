package com.octopus.core.store;

import cn.hutool.json.JSONUtil;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.octopus.core.Request;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/8
 */
public class MongoStore implements Store {

  public static String DEFAULT_DATABASE = "octopus";

  public static String DEFAULT_COLLECTION = "request";

  private static final String STATE_WAITING = "Waiting";

  private static final String STATE_COMPLETED = "Completed";

  private static final String STATE_FAILED = "Failed";

  private static final String STATE_EXECUTING = "Executing";

  private final MongoClient mongoClient;

  private final MongoCollection<Document> requests;

  public MongoStore(String database, String collection, MongoClient mongoClient) {
    this.mongoClient = mongoClient;
    MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
    this.requests = mongoDatabase.getCollection(collection, Document.class);
    IndexOptions options = new IndexOptions();
    requests.createIndex(Indexes.ascending("priority"), new IndexOptions().name("idx_priority"));
  }

  public MongoStore(MongoClient mongoClient) {
    this(DEFAULT_DATABASE, DEFAULT_COLLECTION, mongoClient);
  }

  public MongoStore(String database, String collection) {
    this(
        database,
        collection,
        new MongoClient(ServerAddress.defaultHost(), ServerAddress.defaultPort()));
  }

  public MongoStore() {
    this(DEFAULT_DATABASE, DEFAULT_COLLECTION);
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
              .filter(Filters.eq("state", MongoStore.STATE_WAITING))
              .sort(Sorts.descending("priority"))
              .limit(1)
              .first();
      if (request == null) {
        request =
            this.requests
                .find()
                .filter(Filters.eq("state", MongoStore.STATE_WAITING))
                .sort(Sorts.descending("priority"))
                .limit(1)
                .first();
        if (request != null) {
          this.requests.updateOne(
              Filters.eq("_id", request.get("_id")), Updates.set("state", STATE_EXECUTING));
        }
      }
      session.commitTransaction();
    } catch (Exception e) {
      e.printStackTrace();
      session.abortTransaction();
    } finally {
      session.close();
    }
    return request == null ? null : JSONUtil.toBean(request.toJson(), Request.class);
  }

  @Override
  public boolean put(Request request) {
    Document document = Document.parse(JSONUtil.toJsonStr(request));
    String id = request.getId();
    document.put("_id", id);
    document.put("state", MongoStore.STATE_WAITING);
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
    this.requests.drop();
  }

  @Override
  public void markAsCompleted(Request request) {
    Bson filter = Filters.eq("_id", request.getId());
    this.requests.updateOne(filter, Updates.set("state", MongoStore.STATE_COMPLETED));
  }

  @Override
  public void markAsFailed(Request request) {
    Bson filter = Filters.eq("_id", request.getId());
    this.requests.updateOne(filter, Updates.set("state", MongoStore.STATE_FAILED));
  }

  @Override
  public long getTotalSize() {
    return this.requests.countDocuments();
  }

  @Override
  public long getCompletedSize() {
    return this.requests.countDocuments(Filters.eq("state", MongoStore.STATE_COMPLETED));
  }

  @Override
  public long getWaitingSize() {
    return this.requests.countDocuments(Filters.eq("state", MongoStore.STATE_WAITING));
  }

  @Override
  public List<Request> getFailed() {
    List<Request> failed = new ArrayList<>();
    for (Document doc : this.requests.find(Filters.eq("state", MongoStore.STATE_FAILED))) {
      failed.add(JSONUtil.toBean(doc.toJson(), Request.class));
    }
    return failed;
  }

  @Override
  public void clearFailed() {
    this.requests.deleteMany(Filters.eq("state", MongoStore.STATE_FAILED));
  }
}
