package com.octopus.core.processor.extractor.convertor;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.ConvertException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
@Slf4j
public class IntegerConvertorHandler implements ConvertorHandler<Integer, IntegerConvertor> {

  @Override
  public Integer convert(String val, IntegerConvertor format, Response response)
      throws ConvertException {
    if (StrUtil.isBlank(val)) {
      return format.def();
    }
    try {
      return NumberUtil.parseInt(val);
    } catch (Exception e) {
      if (!format.ignorable()) {
        throw new ConvertException(val, Integer.class);
      }
    }
    return null;
  }
}
