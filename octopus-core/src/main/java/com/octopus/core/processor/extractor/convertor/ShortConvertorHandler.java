package com.octopus.core.processor.extractor.convertor;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.ConvertException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
@Slf4j
public class ShortConvertorHandler implements ConvertorHandler<Short, ShortConvertor> {

  @Override
  public Short convert(String val, ShortConvertor format, Response response)
      throws ConvertException {
    if (StrUtil.isBlank(val)) {
      return format.def();
    }
    try {
      return Short.parseShort(val);
    } catch (Exception e) {
      if (!format.ignorable()) {
        throw new ConvertException(val, Short.class);
      }
    }
    return null;
  }
}
