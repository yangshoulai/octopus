package com.octopus.core.processor.collector;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.processor.Collector;
import com.octopus.core.processor.jexl.Jexl;
import com.octopus.core.processor.jexl.JexlHelper;
import com.octopus.core.properties.collector.CollectorTarget;
import com.octopus.core.properties.collector.DownloaderCollectorProperties;
import lombok.Data;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

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
        Map<String, Object> ctx = JexlHelper.buildContext(result, response);
        String dir = Objects.requireNonNull(Jexl.eval(properties.getDir(), ctx)).toString();
        String fileName = null;
        if (StrUtil.isNotBlank(properties.getName())) {
            Object r = Jexl.eval(properties.getName(), ctx);
            if (r != null) {
                fileName = r.toString();
            } else {
                fileName = getFileNameFromDisposition(response.getHeaders());
            }
        }
        if (StrUtil.isBlank(fileName)) {
            fileName = FileUtil.getName(response.getRequest().getUrl());
        }
        File targetFile = new File(dir, fileName);
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
