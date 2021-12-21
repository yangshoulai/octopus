package com.octopus.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Data;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/12/21
 */
public class LRUCache<K, V> {

  private final int capacity;

  private final Map<K, CacheNode<K, V>> map = new HashMap<>();

  private final ReentrantLock lock = new ReentrantLock();

  private CacheNode<K, V> head;

  private CacheNode<K, V> tail;

  public LRUCache(int capacity) {
    this.capacity = capacity;
  }

  public V get(K k) {
    lock.lock();
    try {
      CacheNode<K, V> node = this.map.get(k);
      if (node != null) {
        this.setTail(node);
      }
      return node == null ? null : node.getValue();
    } finally {
      lock.unlock();
    }
  }

  public void put(@NonNull K k, V v) {
    lock.lock();
    try {
      CacheNode<K, V> node = new CacheNode<>(k, v);
      this.setTail(node);
      if (this.head == null) {
        this.head = node;
      }
      int size = this.map.size();
      if (size >= this.capacity) {
        CacheNode<K, V> removeNode = this.head;
        this.head = removeNode.nextNode;
        this.head.setPreNode(null);
        this.removeNode(removeNode);
        this.map.remove(removeNode.getKey());
      }
      this.map.put(k, node);
    } finally {
      lock.unlock();
    }
  }

  public int size() {
    return this.map.size();
  }

  public boolean exists(K k) {
    return this.map.containsKey(k);
  }

  private void setTail(CacheNode<K, V> node) {
    if (this.tail != node) {
      this.removeNode(node);
      if (this.tail != null) {
        this.tail.setNextNode(node);
      }
      node.setPreNode(this.tail);
      this.tail = node;
    }
  }

  private void removeNode(CacheNode<K, V> node) {
    CacheNode<K, V> preNode = node.getPreNode();
    CacheNode<K, V> nextNode = node.getNextNode();
    if (preNode != null) {
      preNode.setNextNode(nextNode);
    }
    if (nextNode != null) {
      nextNode.setPreNode(preNode);
    }
    node.setPreNode(null);
    node.setNextNode(null);
  }

  @Data
  static class CacheNode<K, V> {
    private K key;

    private V value;

    private CacheNode<K, V> preNode;

    private CacheNode<K, V> nextNode;

    public CacheNode(K key, V value) {
      this.key = key;
      this.value = value;
    }
  }
}
