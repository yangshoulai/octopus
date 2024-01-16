package com.octopus.core.processor;

import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
@Data
public class FieldInfo {

  private boolean isArray;

  private boolean isCollection;

  private Class<?> collectionClass;

  private Class<?> componentClass;

}
