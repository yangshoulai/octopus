package com.octopus.core.processor.extractor;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.annotation.Extractor;
import com.octopus.core.processor.extractor.annotation.Link;
import com.octopus.core.processor.extractor.annotation.LinkMethod;
import com.octopus.core.processor.extractor.annotation.Prop;
import com.octopus.core.processor.extractor.selector.Selector;
import com.octopus.core.processor.extractor.type.TypeHandler;
import com.octopus.core.processor.extractor.type.TypeHandlerRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class Validator {

  private final Set<Class<?>> VALID = new HashSet<>();

  private final Set<Class<?>> VALIDATING = new HashSet<>();

  private Validator() {}

  public static Validator getInstance() {
    return Holder.INSTANCE;
  }

  private static class Holder {
    public static Validator INSTANCE = new Validator();
  }

  public void validate(Class<?> extractorClass) throws ValidateException {
    if (!VALID.contains(extractorClass)) {
      if (!extractorClass.isAnnotationPresent(Extractor.class)) {
        throw new ValidateException("Extractor does not has @Extractor annotation");
      }
      VALIDATING.add(extractorClass);
      List<Field> fields = this.getSelectorFields(extractorClass);
      for (Field field : fields) {
        this.validate(field);
      }

      List<Method> methods = this.getLinkMethods(extractorClass);
      for (Method method : methods) {
        this.validate(method);
      }

      Link[] links = extractorClass.getAnnotationsByType(Link.class);
      for (Link link : links) {
        this.validate(extractorClass, link);
      }

      VALIDATING.remove(extractorClass);
      VALID.add(extractorClass);
    }
  }

  public void validate(Class<?> extractorClass, Link link) {
    if (link.url().length <= 0 && link.selectors().length <= 0) {
      throw new ValidateException("Invalid @Link annotation, url or selectors must be specified");
    }
    Prop[] props = ArrayUtil.addAll(link.attrs(), link.headers(), link.params());
    for (Prop prop : props) {
      this.validate(extractorClass, prop);
    }
  }

  public void validate(Class<?> extractorClass, Prop prop) {
    if (StrUtil.isNotBlank(prop.field())) {
      Field field = ReflectUtil.getField(extractorClass, prop.field());
      if (field == null) {
        throw new InvalidExtractorException(
            "Invalid @Prop annotation, filed "
                + prop.field()
                + " not found on Class "
                + extractorClass.getName());
      }
      Class<?> filedType = TypeUtil.getClass(TypeUtil.getType(field));
      if (!CharSequence.class.isAssignableFrom(filedType) && !ClassUtil.isBasicType(filedType)) {
        throw new InvalidExtractorException(
            "Invalid @Prop annotation, field type "
                + filedType.getName()
                + " not supported, only support basic type or String");
      }
    }
  }

  public void validate(Field field) throws ValidateException {
    Class<?> type = field.getType();
    FieldType fieldType = ExtractorHelper.getFieldType(field);

    Class<?> componentType = null;
    if (fieldType.isArray()) {
      componentType = fieldType.getComponentClass();
    } else if (fieldType.isCollection()) {
      if (!TypeHandlerRegistry.getInstance()
          .isValidCollectionType(fieldType.getCollectionClass())) {
        throw new ValidateException(
            "Collection type only support java.util.List, java.util.ArrayList, java.util.Set or java.util.HashSet");
      }
      componentType = fieldType.getComponentClass();
    } else {
      componentType = type;
    }
    if (componentType == null) {
      throw new ValidateException("Can not get real type on field type: " + type + "@" + field);
    }

    if (componentType.isAnnotationPresent(Extractor.class)) {
      if (!VALID.contains(componentType) && !VALIDATING.contains(componentType)) {
        validate(componentType);
      }

    } else {
      TypeHandler<?> typeHandler = TypeHandlerRegistry.getInstance().getTypeHandler(componentType);
      if (typeHandler == null) {
        throw new ValidateException("No type handler found for type " + componentType);
      }
    }
  }

  public void validate(Method method) throws ValidateException {
    Class<?> returnType = method.getReturnType();
    if (!String.class.isAssignableFrom(returnType)
        && !Request.class.equals(returnType)
        && !Collection.class.isAssignableFrom(returnType)
        && !returnType.isArray()) {
      throw new ValidateException(
          "Return type on method " + method + " must be String, Request, Collection or Array");
    }
    if (returnType.isArray()) {
      Class<?> componentType = returnType.getComponentType();
      if (!String.class.isAssignableFrom(componentType) && !Request.class.equals(componentType)) {
        throw new ValidateException(
            "Component type of Return type on method " + method + " must be String or Request");
      }
    }
    if (Collection.class.isAssignableFrom(returnType)) {
      Class<?> componentType =
          ExtractorHelper.getCollectionComponentType(method.getGenericReturnType());
      if (componentType == null) {
        throw new ValidateException(
            "Can not determine component type of Return type on method " + method);
      }
      if (!String.class.isAssignableFrom(componentType) && !Request.class.equals(componentType)) {
        throw new ValidateException(
            "Component type of Return type on method " + method + " must be String or Request");
      }
    }
    Class<?>[] paramTypes = method.getParameterTypes();
    if (paramTypes.length > 1) {
      throw new ValidateException(
          "Method " + method + " can only have one parameter with type Response at most");
    }
    if (paramTypes.length == 1 && !Response.class.equals(paramTypes[0])) {
      throw new ValidateException(
          "Method " + method + " can only have one parameter with type Response at most");
    }
  }

  public List<Field> getSelectorFields(Class<?> cls) {
    return ExtractorHelper.getFieldsByAnnotation(cls, Selector.class);
  }

  public List<Method> getLinkMethods(Class<?> cls) {
    return ExtractorHelper.getMethodsByAnnotation(cls, LinkMethod.class);
  }
}
