package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.processor.extractor.configurable.FormatterProperties;
import com.octopus.core.processor.extractor.configurable.SelectorProperties;
import com.octopus.core.exception.SelectException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public abstract class AbstractFieldSelector implements FieldSelector {

    @Override
    public List<String> select(String source, boolean multi, SelectorProperties selector, Response response) throws SelectException {
        try {
            List<String> selected;
            if (multi) {
                selected = this.doMultiSelect(source, selector, response);
            } else {
                selected = ListUtil.of(this.doSingleSelect(source, selector, response));
            }
            if (selected == null) {
                selected = ListUtil.of(selector.getDef());
            }
            selected = selected.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (selector.getFormatter() != null) {
                selected = this.applyFormatter(selector.getFormatter(), selected);
            }
            return multi || selected.isEmpty()
                    ? ListUtil.unmodifiable(selected)
                    : ListUtil.of(selected.get(0));
        } catch (Exception e) {
            throw new SelectException("Select exception from source [" + source + "] with selector [type = " + selector.getType() + ", value = " + selector.getValue() + "]", e);
        }
    }

    private List<String> applyFormatter(FormatterProperties formatter, List<String> selected) {

        if (formatter.isSplit()) {
            selected =
                    selected.stream()
                            .flatMap(s -> Arrays.stream(s.split(formatter.getSeparator())))
                            .collect(Collectors.toList());
        }
        if (formatter.isFilter()) {
            selected = selected.stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
        }
        if (formatter.isTrim()) {
            selected = selected.stream().map(StrUtil::trim).collect(Collectors.toList());
        }
        if (StrUtil.isNotBlank(formatter.getRegex())) {
            selected =
                    selected.stream()
                            .map(
                                    s -> {
                                        int[] groups =
                                                formatter.getGroups() == null || formatter.getGroups().length <= 0
                                                        ? new int[]{0}
                                                        : formatter.getGroups();
                                        List<String> args = new ArrayList<>();
                                        for (int group : groups) {
                                            String groupVal = ReUtil.get(formatter.getRegex(), s, group);
                                            args.add(groupVal == null ? "" : groupVal);
                                        }
                                        return String.format(formatter.getFormat(), args.toArray());
                                    })
                            .collect(Collectors.toList());
        }
        return selected;
    }

    protected abstract List<String> doMultiSelect(String source, SelectorProperties selector, Response response);

    protected String doSingleSelect(String source, SelectorProperties selector, Response response)
            throws SelectException {
        List<String> result = this.doMultiSelect(source, selector, response);
        return result == null || result.isEmpty() ? null : result.get(0);
    }
}
