package com.octopus.core.processor.extractor;

import java.lang.reflect.Type;
import lombok.Data;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/4/27
 */
@Data
class ConvertType {

  private Type actualType;

  private boolean isArray;

  private boolean isCollection;

  private Class<?> collectionClass;

  private Class<?> componentType;
}
