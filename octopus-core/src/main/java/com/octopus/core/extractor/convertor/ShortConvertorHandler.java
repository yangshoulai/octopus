package com.octopus.core.extractor.convertor;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.exception.OctopusException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
@Slf4j
public class ShortConvertorHandler implements ConvertorHandler<Short, ShortConvertor> {

  @Override
  public Short convert(String val, ShortConvertor format) {
    if (StrUtil.isBlank(val)) {
      return format != null ? format.def() : null;
    }
    try {
      return Short.parseShort(val);
    } catch (Exception e) {
      if (format != null && !format.ignorable()) {
        throw new OctopusException(e);
      }
      log.debug("", e);
    }
    return null;
  }

  @Override
  public Class<?>[] getSupportClasses() {
    return new Class[] {short.class, Short.class};
  }
}
