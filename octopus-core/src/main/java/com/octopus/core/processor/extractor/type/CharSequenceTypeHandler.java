package com.octopus.core.processor.extractor.type;

import com.octopus.core.processor.extractor.FieldExt;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/28
 */
public class CharSequenceTypeHandler implements TypeHandler<CharSequence> {

    @Override
    public CharSequence handle(String source, FieldExt ext) {
        return source;
    }

}
