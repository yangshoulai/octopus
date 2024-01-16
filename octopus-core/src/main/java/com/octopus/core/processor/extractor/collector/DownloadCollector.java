package com.octopus.core.processor.extractor.collector;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Response;
import com.octopus.core.configurable.CollectorProperties;
import com.octopus.core.configurable.CollectorTarget;
import com.octopus.core.configurable.SelectorProperties;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.processor.extractor.Collector;
import com.octopus.core.processor.extractor.SelectorRegistry;
import com.octopus.core.processor.extractor.annotation.Selector;
import lombok.Data;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/14
 */
@Data
public class DownloadCollector<R> implements Collector<R> {

    private static SelectorProperties DEFAULT_NAME_SELECTOR = new SelectorProperties(Selector.Type.Url);

    static {
        DEFAULT_NAME_SELECTOR.getFormatter().setRegex("^.*/([^/\\?]+)[^/]*$");
        DEFAULT_NAME_SELECTOR.getFormatter().setGroups(new int[]{1});
    }

    private final CollectorProperties properties;

    public DownloadCollector(CollectorProperties properties) {
        this.properties = properties;
    }

    @Override
    public void collect(R result, Response response) {
        String fileDir = getFileSubDir(response);
        String fileName = null;
        SelectorProperties nameSelector = this.properties.getName() == null ? DEFAULT_NAME_SELECTOR : this.properties.getName();
        if (nameSelector != null) {
            List<String> selected = SelectorRegistry.getInstance().select(nameSelector, response.asText(), false, response);
            if (selected != null && !selected.isEmpty()) {
                fileName = selected.get(0);
            }
        }
        if (StrUtil.isBlank(fileName)) {
            fileName = getFileNameFromDisposition(response.getHeaders());
        }
        if (StrUtil.isBlank(fileName)) {
            fileName = response.getRequest().getId();
        }

        File targetDir = new File(fileDir);
        File targetFile = new File(targetDir, fileName);

        byte[] bytes = null;
        if (this.properties.getTarget() == CollectorTarget.Result && result != null) {
            if (this.properties.isPretty()) {
                bytes = JSONUtil.toJsonPrettyStr(result).getBytes(StandardCharsets.UTF_8);
            } else {
                bytes = JSONUtil.toJsonStr(result).getBytes(StandardCharsets.UTF_8);
            }
        } else if (this.properties.getTarget() == CollectorTarget.Body) {
            bytes = response.getBody();
        }

        if (bytes != null) {
            try {
                FileUtil.writeBytes(bytes, targetFile);
            } catch (Exception e) {
                throw new ProcessException(
                        String.format("Can not save file [%s]", targetFile), e);
            }
        }
    }

    public String getFileSubDir(Response response) {
        if (this.properties.getDirs() != null && this.properties.getDirs().length > 0) {
            return Arrays.stream(this.properties.getDirs()).filter(Objects::nonNull).flatMap(selector ->
                            SelectorRegistry.getInstance().select(selector, response.asText(), false, response).stream())
                    .filter(StrUtil::isNotBlank).collect(Collectors.joining(File.separator));
        }
        return null;
    }

    /**
     * 从Content-Disposition头中获取文件名
     *
     * @return 文件名，empty表示无
     */
    private String getFileNameFromDisposition(Map<String, String> headers) {
        String fileName = null;
        if (headers != null) {
            final String disposition = headers.get("Content-Disposition");
            if (StrUtil.isNotBlank(disposition)) {
                fileName = ReUtil.get("filename=\"(.*?)\"", disposition, 1);
                if (StrUtil.isBlank(fileName)) {
                    fileName = StrUtil.subAfter(disposition, "filename=", true);
                }
            }
        }
        return fileName;
    }
}
