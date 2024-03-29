package com.octopus.core.processor.selector;

import com.octopus.core.Response;
import com.octopus.core.properties.selector.RegexSelectorProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/30
 */
public class RegexSelector extends AbstractCacheableSelector<String, RegexSelectorProperties> {
    @Override
    protected List<String> doSelectWithDoc(
            String content, RegexSelectorProperties selector, boolean multi, Response response) {
        List<String> list = new ArrayList<>();
        int[] groups = selector.getGroups();
        String format = selector.getFormat();
        Pattern pattern = Pattern.compile(selector.getExpression());
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            List<String> args = new ArrayList<>();
            for (int group : groups) {
                args.add(matcher.group(group));
            }
            list.add(String.format(format, args.toArray()));
            if (!multi) {
                break;
            }
        }
        return list;
    }

    @Override
    protected String parse(String content) {
        return content;
    }
}
