package com.octopus.core.processor.extractor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/3/27
 */
public abstract class AbstractSelectorHandler implements SelectorHandler {

  @Override
  public List<String> select(String source, Selector selector, boolean multi, Response response)
      throws SelectException {

    List<String> selected;
    if (multi) {
      selected = this.doMultiSelect(source, selector, response);
    } else {
      selected = ListUtil.of(this.doSingleSelect(source, selector, response));
    }
    if (selected == null) {
      selected = ListUtil.of(selector.def());
    }
    selected = selected.stream().filter(Objects::nonNull).collect(Collectors.toList());
    if (selector.formatter() != null) {
      selected = this.applyFormatter(selector.formatter(), selected);
    }
    return multi || selected.isEmpty()
        ? ListUtil.unmodifiable(selected)
        : ListUtil.of(selected.get(0));
  }

  private List<String> applyFormatter(Formatter formatter, List<String> selected) {

    if (formatter.split()) {
      selected =
          selected.stream()
              .flatMap(s -> Arrays.stream(s.split(formatter.separator())))
              .collect(Collectors.toList());
    }
    if (formatter.filter()) {
      selected = selected.stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
    }
    if (formatter.trim()) {
      selected = selected.stream().map(StrUtil::trim).collect(Collectors.toList());
    }
    if (StrUtil.isNotBlank(formatter.regex())) {
      selected =
          selected.stream()
              .map(
                  s -> {
                    int[] groups =
                        formatter.groups() == null || formatter.groups().length <= 0
                            ? new int[] {0}
                            : formatter.groups();
                    List<String> args = new ArrayList<>();
                    for (int group : groups) {
                      String groupVal = ReUtil.get(formatter.regex(), s, group);
                      args.add(groupVal == null ? "" : groupVal);
                    }
                    return String.format(formatter.format(), args.toArray());
                  })
              .collect(Collectors.toList());
    }
    return selected;
  }

  /**
   * 从文本中选择内容
   *
   * @param source 文本
   * @param selector 选择器
   * @param response 请求响应
   * @return 选择内容
   * @throws SelectException 获取内容异常
   */
  protected abstract List<String> doMultiSelect(String source, Selector selector, Response response)
      throws SelectException;

  /**
   * 从文本中选择内容
   *
   * @param source 文本
   * @param selector 选择器
   * @param response 请求响应
   * @return 选择内容
   * @throws SelectException 获取内容异常
   */
  protected String doSingleSelect(String source, Selector selector, Response response)
      throws SelectException {
    List<String> result = this.doMultiSelect(source, selector, response);
    return result == null || result.isEmpty() ? null : result.get(0);
  }
}
