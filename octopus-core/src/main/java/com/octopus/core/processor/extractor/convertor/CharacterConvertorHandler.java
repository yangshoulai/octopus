package com.octopus.core.processor.extractor.convertor;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/24
 */
public class CharacterConvertorHandler implements ConvertorHandler<Character, CharacterConvertor> {

  @Override
  public Character convert(String val, CharacterConvertor format, Response response) {
    if (StrUtil.isBlank(val)) {
      return null;
    }
    return val.charAt(0);
  }

  @Override
  public Class<?>[] getSupportClasses() {
    return new Class[] {boolean.class, Boolean.class};
  }
}
