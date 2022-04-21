package com.octopus.core.processor.extractor.convertor;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.ConvertException;
import java.util.HashSet;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class BooleanConvertorHandler implements ConvertorHandler<Boolean, BooleanConvertor> {

  @Override
  public Boolean convert(String val, BooleanConvertor format, Response response)
      throws ConvertException {
    if (StrUtil.isBlank(val)) {
      return format.def();
    }
    return !new HashSet<>(ListUtil.of(format.falseValues())).contains(val.toLowerCase());
  }
}
