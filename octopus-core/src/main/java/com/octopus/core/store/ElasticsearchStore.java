package com.octopus.core.store;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.octopus.core.Request;
import com.octopus.core.Request.State;
import com.octopus.core.Request.Status;
import com.octopus.core.exception.OctopusException;
import com.octopus.core.replay.ReplayFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/4/13
 */
public class ElasticsearchStore implements Store {

  private final String indexName;

  private final ElasticsearchClient client;

  public ElasticsearchStore(@NonNull ElasticsearchClient client, @NonNull String indexName) {
    this.client = client;
    this.indexName = indexName;
    try {
      this.createIndexIfNeed();
    } catch (IOException e) {
      throw new OctopusException("Create index [" + indexName + "] failed", e);
    }
    try {
      this.replayExecutingRequests();
    } catch (IOException e) {
      throw new OctopusException("Replay executing request failed", e);
    }
  }

  private void replayExecutingRequests() throws IOException {
    int size = 100;
    boolean hasMore;
    do {
      List<Request> requests = this.batchSearchByState(0, size, State.Executing);
      hasMore = requests.size() >= size;
      if (!requests.isEmpty()) {
        List<BulkOperation> operations = new ArrayList<>();
        for (Request request : requests) {
          request.setStatus(Status.of(State.Waiting));
          IndexOperation<Request> operation =
              IndexOperation.of(f -> f.index(indexName).document(request).id(request.getId()));
          operations.add(BulkOperation.of(f -> f.index(operation)));
        }
        client.bulk(f -> f.refresh(Refresh.True).operations(operations));
      }
    } while (hasMore);
  }

  private void createIndexIfNeed() throws IOException {
    if (!this.client.indices().exists(f -> f.index(indexName)).value()) {
      // @formatter:off
      CreateIndexResponse response =
          client
              .indices()
              .create(
                  f ->
                      f.index(this.indexName)
                          .mappings(
                              mappingBuilder ->
                                  mappingBuilder
                                      .properties(
                                          "id", propertyBuilder -> propertyBuilder.keyword(b -> b))
                                      .properties(
                                          "url", propertyBuilder -> propertyBuilder.text(b -> b))
                                      .properties(
                                          "method",
                                          propertyBuilder -> propertyBuilder.keyword(b -> b))
                                      .properties(
                                          "priority",
                                          propertyBuilder -> propertyBuilder.integer(b -> b))
                                      .properties(
                                          "repeatable",
                                          propertyBuilder -> propertyBuilder.boolean_(b -> b))
                                      .properties(
                                          "parent",
                                          propertyBuilder -> propertyBuilder.keyword(b -> b))
                                      .properties(
                                          "inherit",
                                          propertyBuilder -> propertyBuilder.boolean_(b -> b))
                                      .properties(
                                          "status.state",
                                          propertyBuilder -> propertyBuilder.keyword(b -> b))
                                      .properties(
                                          "status.message",
                                          propertyBuilder -> propertyBuilder.text(b -> b))
                                      .properties(
                                          "failTimes",
                                          propertyBuilder -> propertyBuilder.integer(b -> b))));
      // @formatter:on
      if (!response.acknowledged()) {
        throw new OctopusException("Create index [" + indexName + "] failed");
      }
    }
  }

  @Override
  public Request get() {
    try {
      TermQuery termQuery = TermQuery.of(t -> t.field("status.state").value(State.Waiting.name()));
      Query filter = Query.of(q -> q.term(termQuery));
      Query query = Query.of(q -> q.bool(b -> b.filter(filter)));
      SortOptions sortOptions =
          SortOptions.of(s -> s.field(fs -> fs.field("priority").order(SortOrder.Desc)));
      SearchRequest request =
          SearchRequest.of(f -> f.index(this.indexName).query(query).sort(sortOptions).size(1));
      SearchResponse<Request> response = client.search(request, Request.class);
      List<Hit<Request>> hits = response.hits().hits();
      if (hits != null && !hits.isEmpty()) {
        Request r = hits.get(0).source();
        if (r != null) {
          r.setStatus(Status.of(State.Executing));
          UpdateRequest<Request, Request> updateRequest =
              UpdateRequest.of(
                  f -> f.index(indexName).id(r.getId()).doc(r).upsert(r).refresh(Refresh.True));
          client.update(updateRequest, Request.class);
          return r;
        }
      }
    } catch (Exception e) {
      throw new OctopusException("Get index failed", e);
    }
    return null;
  }

  @Override
  public boolean put(Request request) {
    try {
      client.index(
          i -> i.index(this.indexName).id(request.getId()).document(request).refresh(Refresh.True));
      return true;
    } catch (IOException e) {
      throw new OctopusException("Put index failed", e);
    }
  }

  @Override
  public boolean exists(Request request) {
    try {
      return client.exists(i -> i.index(this.indexName).id(request.getId())).value();
    } catch (IOException e) {
      throw new OctopusException("Exists index failed", e);
    }
  }

  @Override
  public void clear() {
    try {
      this.client.delete(i -> i.index(this.indexName));
    } catch (IOException e) {
      throw new OctopusException("Delete index failed", e);
    }
  }

  @Override
  public void markAsCompleted(Request request) {
    try {
      request.setStatus(Status.of(State.Completed, null));
      UpdateRequest<Request, Request> req =
          UpdateRequest.of(
              f ->
                  f.index(this.indexName)
                      .upsert(request)
                      .id(request.getId())
                      .doc(request)
                      .refresh(Refresh.True));
      client.update(req, Request.class);
    } catch (IOException e) {
      throw new OctopusException("Update index failed", e);
    }
  }

  @Override
  public void markAsFailed(Request request, String error) {
    try {
      request.setStatus(Status.of(State.Failed, error));
      UpdateRequest<Request, Request> req =
          UpdateRequest.of(
              f ->
                  f.index(this.indexName)
                      .upsert(request)
                      .id(request.getId())
                      .doc(request)
                      .refresh(Refresh.True));
      client.update(req, Request.class);
    } catch (IOException e) {
      throw new OctopusException("Update index failed", e);
    }
  }

  @Override
  public long getTotalSize() {
    try {
      return client.count(f -> f.index(this.indexName)).count();
    } catch (IOException e) {
      throw new OctopusException("Count index failed", e);
    }
  }

  @Override
  public long getCompletedSize() {
    try {
      TermQuery termQuery =
          TermQuery.of(t -> t.field("status.state").value(State.Completed.name()));
      Query filter = Query.of(q -> q.term(termQuery));
      Query query = Query.of(q -> q.bool(b -> b.filter(filter)));

      return client.count(f -> f.index(this.indexName).query(query)).count();
    } catch (IOException e) {
      throw new OctopusException("Count index failed", e);
    }
  }

  @Override
  public long getWaitingSize() {
    TermQuery termQuery = TermQuery.of(t -> t.field("status.state").value(State.Waiting.name()));
    Query filter = Query.of(q -> q.term(termQuery));
    Query query = Query.of(q -> q.bool(b -> b.filter(filter)));
    try {
      return client.count(f -> f.index(this.indexName).query(query)).count();
    } catch (IOException e) {
      throw new OctopusException("Count index failed", e);
    }
  }

  @Override
  public long getFailedSize() {
    TermQuery termQuery = TermQuery.of(t -> t.field("status.state").value(State.Failed.name()));
    Query filter = Query.of(q -> q.term(termQuery));
    Query query = Query.of(q -> q.bool(b -> b.filter(filter)));
    try {
      return client.count(f -> f.index(this.indexName).query(query)).count();
    } catch (IOException e) {
      throw new OctopusException("Count index failed", e);
    }
  }

  @Override
  public List<Request> getFailed() {
    int from = 0;
    int size = 100;
    boolean hasMore = false;
    List<Request> results = new ArrayList<>();
    do {
      List<Request> requests = this.batchSearchByState(from, size, State.Failed);
      hasMore = requests.size() >= size;
      from += requests.size();
      results.addAll(requests);
    } while (hasMore);
    return results;
  }

  @Override
  public void delete(String id) {
    try {
      this.client.delete(dq -> dq.index(this.indexName).id(id).refresh(Refresh.True));
    } catch (IOException e) {
      throw new OctopusException("Deleted index failed", e);
    }
  }

  @Override
  public int replayFailed(ReplayFilter filter) {
    int from = 0;
    int size = 100;
    List<String> toDeleted = new ArrayList<>();
    boolean hasMore = false;
    do {
      List<Request> requests = this.batchSearchByState(from, size, State.Failed);
      hasMore = requests.size() >= size;
      from += requests.size();
      for (Request request : requests) {
        if (filter.filter(request)) {
          toDeleted.add(request.getId());
        }
      }
    } while (hasMore);

    if (!toDeleted.isEmpty()) {
      Script script =
          new Script.Builder()
              .inline(
                  f ->
                      f.source(
                          "ctx._source.status.state='"
                              + State.Waiting.name()
                              + "';ctx._source.status.message=null;ctx._source.failTimes++;"))
              .build();
      try {
        this.client.updateByQuery(
            f ->
                f.index(indexName)
                    .query(q -> q.ids(ids -> ids.values(toDeleted)))
                    .script(script)
                    .refresh(true));
      } catch (IOException e) {
        throw new OctopusException("Update index failed", e);
      }
    }
    return toDeleted.size();
  }

  public List<Request> batchSearchByState(int from, int size, State state) {
    try {
      TermQuery termQuery = TermQuery.of(t -> t.field("status.state").value(state.name()));
      Query filter = Query.of(q -> q.term(termQuery));
      Query query = Query.of(q -> q.bool(b -> b.filter(filter)));
      SearchRequest request =
          SearchRequest.of(f -> f.index(indexName).query(query).from(from).size(size));
      SearchResponse<Request> response = this.client.search(request, Request.class);
      return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    } catch (IOException e) {
      throw new OctopusException("Search index failed", e);
    }
  }
}
