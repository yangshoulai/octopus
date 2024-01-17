package com.octopus.core.processor.selector;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.SelectException;
import com.octopus.core.processor.Selector;
import com.octopus.core.properties.selector.AbstractSelectorProperties;
import com.octopus.core.properties.selector.DenoiserProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
public abstract class AbstractSelector<P extends AbstractSelectorProperties> implements Selector<P> {
  @Override
  public List<String> select(String source, boolean multi, P selector, Response response)
      throws SelectException {
    try {
      List<String> selected;
      if (multi) {
        selected = this.doMultiSelect(source, selector, response);
      } else {
        selected = ListUtil.of(this.doSingleSelect(source, selector, response));
      }
      if (selected == null) {
        selected = ListUtil.of(selector.getDefaultValue());
      }
      selected = selected.stream().filter(Objects::nonNull).collect(Collectors.toList());
      if (selector.getDenoiser() != null) {
        selected = this.applyFormatter(selector.getDenoiser(), selected);
      }
      return multi || selected.isEmpty()
          ? ListUtil.unmodifiable(selected)
          : ListUtil.of(selected.get(0));
    } catch (Exception e) {
      throw new SelectException("error when select from " + response + " with " + selector, e);
    }
  }

  private List<String> applyFormatter(DenoiserProperties denoiser, List<String> selected) {
    if (denoiser.isSplit()) {
      selected =
          selected.stream()
              .flatMap(s -> Arrays.stream(s.split(denoiser.getSeparator())))
              .collect(Collectors.toList());
    }
    if (denoiser.isFilter()) {
      selected = selected.stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
    }
    if (denoiser.isTrim()) {
      selected = selected.stream().map(StrUtil::trim).collect(Collectors.toList());
    }
    if (StrUtil.isNotBlank(denoiser.getRegex())) {
      selected =
          selected.stream()
              .map(
                  s -> {
                    int[] groups =
                        denoiser.getGroups() == null || denoiser.getGroups().length <= 0
                            ? new int[] {0}
                            : denoiser.getGroups();
                    List<String> args = new ArrayList<>();
                    for (int group : groups) {
                      String groupVal = ReUtil.get(denoiser.getRegex(), s, group);
                      args.add(groupVal == null ? "" : groupVal);
                    }
                    return String.format(denoiser.getFormat(), args.toArray());
                  })
              .collect(Collectors.toList());
    }
    return selected;
  }

  protected abstract List<String> doMultiSelect(
          String source, P selector, Response response);

  protected String doSingleSelect(String source, P selector, Response response)
      throws SelectException {
    List<String> result = this.doMultiSelect(source, selector, response);
    return result == null || result.isEmpty() ? null : result.get(0);
  }
}
