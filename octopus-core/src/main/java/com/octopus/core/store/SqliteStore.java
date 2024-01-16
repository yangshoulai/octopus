package com.octopus.core.store;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Request;
import com.octopus.core.replay.ReplayFilter;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/04
 */
public class SqliteStore implements Store {

  public static final String DEFAULT_TABLE_NAME = "requests";
  private final String tableName;

  private final Connection connection;

  private final Lock writeLock = new ReentrantLock();

  public SqliteStore(String databaseFilePath) {
    this(databaseFilePath, DEFAULT_TABLE_NAME);
  }

  public SqliteStore(String databaseFilePath, String tableName) {
    this.tableName = tableName;
    try {
      Class.forName("org.sqlite.JDBC");
      this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath);
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException(e);
    }
    this.initTables();
  }

  private void initTables() {
    Statement statement = null;
    try {
      statement = this.connection.createStatement();
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
          "id TEXT PRIMARY KEY, " +
          "url TEXT, " +
          "method TEXT, " +
          "priority INTEGER, " +
          "repeatable INTEGER, " +
          "parent TEXT, " +
          "inherit INTEGER, " +
          "fails INTEGER, " +
          "state TEXT, " +
          "err TEXT, " +
          "body BLOB, " +
          "params TEXT, " +
          "headers TEXT, " +
          "attrs TEXT" +
          ")");
      statement.execute("CREATE INDEX IF NOT EXISTS idx_" + tableName + "_priority on " + tableName + " (priority)");
      statement.execute("update " + tableName + " set state = '" + Request.State.Waiting + "', err = NULL where state = '" + Request.State.Executing + "'");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

  }

  @Override
  public Request get() {
    Statement statement = null;
    writeLock.lock();
    try {
      connection.setAutoCommit(false);
      statement = this.connection.createStatement();
      Request request = null;
      try (ResultSet resultSet = statement.executeQuery(
          "select * from " + tableName + " where state = '" + Request.State.Waiting + "' order by priority desc limit 1")) {
        List<Request> requests = toRequest(resultSet);
        if (!requests.isEmpty()) {
          request = requests.get(0);
          request.setStatus(Request.Status.of(Request.State.Executing));
          updateStatus(request.getId(), Request.State.Executing, null, request.getFailTimes());
        }
      }
      connection.commit();
      return request;
    } catch (Exception e) {
      try {
        connection.rollback();
      } catch (SQLException ex) {
        // ignore
      }
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ex) {
          // ignore
        }
      }
      throw new RuntimeException(e);
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException e) {
        // ignore
      }
      writeLock.unlock();
    }
  }

  private List<Request> toRequest(ResultSet resultSet) throws SQLException {
    List<Request> requests = new ArrayList<>();
    while (resultSet.next()) {
      Request request = new Request();
      request.setId(resultSet.getString("id"));
      request.setUrl(resultSet.getString("url"));
      request.setMethod(Request.RequestMethod.valueOf(resultSet.getString("method")));
      request.setPriority(resultSet.getInt("priority"));
      request.setRepeatable(resultSet.getInt("repeatable") == 1);
      String parent = resultSet.getString("parent");
      if (StrUtil.isNotBlank(parent)) {
        request.setParent(parent);
      }
      request.setInherit(resultSet.getInt("inherit") == 1);
      request.setFailTimes(resultSet.getInt("fails"));
      request.setStatus(Request.Status.of(Request.State.valueOf(resultSet.getString("state")), resultSet.getString("err")));
      request.setBody(resultSet.getBytes("body"));
      String params = resultSet.getString("params");
      if (StrUtil.isNotBlank(params)) {
        request.setParams(JSONUtil.toBean(params, new TypeReference<HashMap<String, String>>() {
          @Override
          public Type getType() {
            return super.getType();
          }
        }, true));
      }
      String headers = resultSet.getString("headers");
      if (StrUtil.isNotBlank(headers)) {
        request.setHeaders(JSONUtil.toBean(headers, new TypeReference<HashMap<String, String>>() {
          @Override
          public Type getType() {
            return super.getType();
          }
        }, true));
      }
      String attrs = resultSet.getString("attrs");
      if (StrUtil.isNotBlank(attrs)) {
        request.setAttributes(JSONUtil.toBean(attrs, new TypeReference<HashMap<String, Object>>() {
          @Override
          public Type getType() {
            return super.getType();
          }
        }, true));
      }
      requests.add(request);
    }
    return requests;
  }

  @Override
  public boolean put(Request request) {
    String sql = "insert into " + tableName
        + " (id, url, method, priority, repeatable, parent, inherit, fails, state, err, body, params, headers, attrs) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    String updateSql = "update " + tableName
        + " set url =?, method =?, priority =?, repeatable =?, parent =?, inherit =?, fails =?, state =?, err =?, body =?, params =?, headers =?, attrs =? where id =?";
    PreparedStatement statement = null;
    writeLock.lock();
    try {
      connection.setAutoCommit(false);
      if (exists(request)) {
        statement = this.connection.prepareStatement(updateSql);
        statement.setString(14, request.getId());
        statement.setString(1, request.getUrl());
        statement.setString(2, request.getMethod().name());
        statement.setInt(3, request.getPriority());
        statement.setInt(4, request.isRepeatable() ? 1 : 0);
        statement.setString(5, request.getParent());
        statement.setInt(6, request.isInherit() ? 1 : 0);
        statement.setInt(7, request.getFailTimes());
        statement.setString(8, Request.State.Waiting.name());
        statement.setString(9, null);
        statement.setBytes(10, request.getBody());
        if (request.getParams() != null) {
          statement.setString(11, JSONUtil.toJsonStr(request.getParams()));
        } else {
          statement.setString(11, null);
        }
        if (request.getHeaders() != null) {
          statement.setString(12, JSONUtil.toJsonStr(request.getHeaders()));
        } else {
          statement.setString(12, null);
        }
        if (request.getAttributes() != null) {
          statement.setString(13, JSONUtil.toJsonStr(request.getAttributes()));
        } else {
          statement.setString(13, null);
        }
      } else {
        statement = this.connection.prepareStatement(sql);
        statement.setString(1, request.getId());
        statement.setString(2, request.getUrl());
        statement.setString(3, request.getMethod().name());
        statement.setInt(4, request.getPriority());
        statement.setInt(5, request.isRepeatable() ? 1 : 0);
        statement.setString(6, request.getParent());
        statement.setInt(7, request.isInherit() ? 1 : 0);
        statement.setInt(8, request.getFailTimes());
        statement.setString(9, Request.State.Waiting.name());
        statement.setString(10, null);
        statement.setBytes(11, request.getBody());
        if (request.getParams() != null) {
          statement.setString(12, JSONUtil.toJsonStr(request.getParams()));
        } else {
          statement.setString(12, null);
        }
        if (request.getHeaders() != null) {
          statement.setString(13, JSONUtil.toJsonStr(request.getHeaders()));
        } else {
          statement.setString(13, null);
        }
        if (request.getAttributes() != null) {
          statement.setString(14, JSONUtil.toJsonStr(request.getAttributes()));
        } else {
          statement.setString(14, null);
        }
      }
      statement.executeUpdate();
      connection.commit();
      return true;
    } catch (Exception e) {
      try {
        connection.rollback();
      } catch (SQLException ex) {
        // ignore
      }
      throw new RuntimeException(e);
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e1) {
          // ignore
        }
      }
      writeLock.unlock();
    }
  }

  @Override
  public boolean exists(Request request) {
    try (Statement statement = this.connection.createStatement()) {
      try (ResultSet resultSet = statement.executeQuery("select count(1) from " + tableName + " where id = '" + request.getId() + "'")) {
        return resultSet.next() && resultSet.getInt(1) > 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void clear() {
    try (Statement statement = this.connection.createStatement()) {
      statement.execute("DELETE FROM " + tableName);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void markAsCompleted(Request request) {
    updateStatus(request.getId(), Request.State.Completed, null, request.getFailTimes());
  }

  @Override
  public void markAsFailed(Request request, String error) {
    updateStatus(request.getId(), Request.State.Failed, error, request.getFailTimes());
  }

  private void updateStatus(String id, Request.State state, String error, int fails) {
    writeLock.lock();
    try {
      try (PreparedStatement statement = this.connection.prepareStatement("update " + tableName + " set state = ?, err = ?, fails = ? where id = ?")) {
        statement.setString(1, state.name());
        statement.setString(2, error);
        statement.setInt(3, state == Request.State.Failed ? fails + 1 : fails);
        statement.setString(4, id);
        statement.executeUpdate();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public long getTotalSize() {
    try (Statement statement = this.connection.createStatement()) {
      try (ResultSet resultSet = statement.executeQuery("select count(1) from " + tableName)) {
        return resultSet.next() ? resultSet.getLong(1) : 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long getCompletedSize() {
    try (Statement statement = this.connection.createStatement()) {
      try (ResultSet resultSet = statement.executeQuery("select count(1) from " + tableName + " where state = '" + Request.State.Completed + "'")) {
        return resultSet.next() ? resultSet.getLong(1) : 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long getWaitingSize() {
    try (Statement statement = this.connection.createStatement()) {
      try (ResultSet resultSet = statement.executeQuery("select count(1) from " + tableName + " where state = '" + Request.State.Waiting + "'")) {
        return resultSet.next() ? resultSet.getLong(1) : 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long getFailedSize() {
    try (Statement statement = this.connection.createStatement()) {
      try (ResultSet resultSet = statement.executeQuery("select count(1) from " + tableName + " where state = '" + Request.State.Failed + "'")) {
        return resultSet.next() ? resultSet.getLong(1) : 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public void delete(String id) {
    writeLock.lock();
    try {
      try (Statement statement = this.connection.createStatement()) {
        statement.execute("delete from " + tableName + " where id = '" + id + "'");
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public int replayFailed(ReplayFilter filter) {
    writeLock.lock();
    int num = 0;
    try {
      try (Statement statement = this.connection.createStatement()) {
        while (true) {
          ResultSet resultSet = statement.executeQuery("select * from " + tableName + " where state = '" + Request.State.Failed + "' limit 500");
          List<Request> requests = toRequest(resultSet).stream().filter(filter::filter).collect(Collectors.toList());
          num += requests.size();
          for (Request request : requests) {
            this.put(request);
          }
          if (requests.isEmpty()) {
            break;
          }
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
      return num;
    } finally {
      writeLock.unlock();
    }
  }
}
