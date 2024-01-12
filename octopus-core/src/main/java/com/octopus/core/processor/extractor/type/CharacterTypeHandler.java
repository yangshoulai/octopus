package com.octopus.core.processor.extractor.type;

import cn.hutool.core.util.StrUtil;
import com.octopus.core.processor.extractor.FieldExt;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class CharacterTypeHandler implements TypeHandler<Character> {

    @Override
    public Character handle(String source, FieldExt ext) {
        return StrUtil.isBlank(source) ? null : source.charAt(0);
    }

}
