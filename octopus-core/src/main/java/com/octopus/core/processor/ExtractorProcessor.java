package com.octopus.core.processor;

import com.octopus.core.Request;
import com.octopus.core.Response;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.processor.extractor.Collector;
import com.octopus.core.processor.extractor.ExtractorHelper;
import com.octopus.core.processor.extractor.InvalidExtractorException;
import com.octopus.core.processor.extractor.Result;
import com.octopus.core.processor.matcher.Matcher;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;

/**
 * @author shoulai.yang@gmail.com
 * @date 2022/4/19
 */
public class ExtractorProcessor<T> implements Processor {

  private final Class<T> extractorClass;

  private final Matcher matcher;

  private final Collector<T> collector;

  public ExtractorProcessor(@NonNull Class<T> extractorClass) {
    this(
        extractorClass,
        Objects.requireNonNull(ExtractorHelper.extractMatcher(extractorClass)),
        null);
  }

  public ExtractorProcessor(@NonNull Class<T> extractorClass, Collector<T> collector) {
    this(
        extractorClass,
        Objects.requireNonNull(ExtractorHelper.extractMatcher(extractorClass)),
        collector);
  }

  public ExtractorProcessor(
      @NonNull Class<T> extractorClass, @NonNull Matcher matcher, Collector<T> collector) {
    this.extractorClass = extractorClass;
    if (!ExtractorHelper.checkIsValidExtractorClass(extractorClass)) {
      throw new InvalidExtractorException("Not a valid extractor class " + extractorClass);
    }
    this.matcher = matcher;
    this.collector = collector;
  }

  @Override
  public List<Request> process(Response response) {
    try {
      Result<T> result = ExtractorHelper.extract(response, extractorClass);
      if (collector != null) {
        collector.collect(result.getObj());
      }
      return result.getRequests();
    } catch (Exception e) {
      throw new ProcessException(
          "Error process response from request ["
              + response.getRequest()
              + "] with extractor "
              + extractorClass.getName(),
          e);
    }
  }

  @Override
  public boolean matches(Response response) {
    return this.matcher.matches(response);
  }
}
