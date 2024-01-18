package com.octopus.core.processor.collector;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.processor.Collector;
import com.octopus.core.processor.SelectorHelper;
import com.octopus.core.properties.collector.CollectorTarget;
import com.octopus.core.properties.SelectorProperties;
import com.octopus.core.properties.collector.DownloaderCollectorProperties;
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

    private final DownloaderCollectorProperties properties;

    public DownloadCollector(DownloaderCollectorProperties properties) {
        this.properties = properties;
    }

    @Override
    public void collect(R result, Response response) {
        String fileDir = getFileSubDir(response);
        String fileName = null;
        SelectorProperties nameSelector = this.properties.getName();
        List<String> selected = SelectorHelper.getInstance().selectBySelectorProperties(nameSelector, response.asText(), false, response);
        if (selected != null && !selected.isEmpty()) {
            fileName = selected.get(0);
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
                            SelectorHelper.getInstance().selectBySelectorProperties(selector, response.asText(), false, response).stream())
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