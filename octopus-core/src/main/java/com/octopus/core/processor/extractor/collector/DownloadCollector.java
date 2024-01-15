package com.octopus.core.processor.extractor.collector;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Response;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.processor.extractor.Collector;
import lombok.Data;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/14
 */
@Data
public class DownloadCollector<R> implements Collector<R> {

    private boolean downloadResult = true;

    private String dir;

    private String fileDirPropName;

    private String fileNameProdName;

    private boolean useRequestIdAsFileName;

    public DownloadCollector(String dir) {
        this(dir, null, null);
    }

    public DownloadCollector(String dir, String fileDirPropName, String fileNameProdName) {
        this(dir, fileDirPropName, fileNameProdName, true);
    }

    public DownloadCollector(String dir, String fileDirPropName, String fileNameProdName, boolean downloadResult) {
        this.dir = dir;
        this.fileDirPropName = fileDirPropName;
        this.fileNameProdName = fileNameProdName;
        this.downloadResult = downloadResult;
    }

    @Override
    public void collect(R result, Response response) {
        String fileDir = null;
        String fileName = null;
        if (StrUtil.isNotBlank(fileDirPropName)) {
            fileDir = response.getRequest().getAttribute(fileDirPropName);
        }
        if (StrUtil.isNotBlank(fileNameProdName)) {
            fileName = response.getRequest().getAttribute(fileNameProdName);
        }

        if (StrUtil.isBlank(fileName)) {
            fileName = getFileNameFromDisposition(response.getHeaders());
        }
        if (this.useRequestIdAsFileName) {
            fileName = response.getRequest().getId();
        } else {
            if (StrUtil.isBlank(fileName)) {
                fileName = FileUtil.getName(response.getRequest().getUrl());
            }
        }
        File targetDir = new File(dir);
        if (StrUtil.isNotBlank(fileDir)) {
            targetDir = new File(dir, fileDir);
        }
        File targetFile = new File(targetDir, fileName);

        byte[] bytes = null;
        if (downloadResult && result != null) {
            bytes = JSONUtil.toJsonStr(result).getBytes(StandardCharsets.UTF_8);
        } else if (!downloadResult) {
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
