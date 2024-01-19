package com.octopus.core.processor.impl;

import com.octopus.core.processor.Processor;
import com.octopus.core.processor.matcher.Matcher;

import java.util.Properties;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/19
 */
public abstract class AbstractCustomProcessor extends MatchableProcessor {
    protected Properties conf;

    public AbstractCustomProcessor(Matcher matcher, Properties conf) {
        super(matcher);
        this.conf = conf;
    }
}
