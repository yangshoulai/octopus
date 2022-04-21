package com.octopus.core.processor.extractor.convertor;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.ConvertException;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class CharacterConvertorHandler implements ConvertorHandler<Character, CharacterConvertor> {

  @Override
  public Character convert(String val, CharacterConvertor format, Response response)
      throws ConvertException {
    if (StrUtil.isBlank(val)) {
      return null;
    }
    return val.charAt(0);
  }
}
