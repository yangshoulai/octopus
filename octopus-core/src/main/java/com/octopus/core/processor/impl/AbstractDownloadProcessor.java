package com.octopus.core.processor.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Response;
import com.octopus.core.exception.ProcessException;
import com.octopus.core.processor.matcher.Matcher;
import lombok.NonNull;

import java.io.File;
import java.util.Map;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/22
 */
public abstract class AbstractDownloadProcessor extends MatchableProcessor {

    private static final int MAX_FILE_PATH_LENGTH = 255;

    public AbstractDownloadProcessor(@NonNull Matcher matcher) {
        super(matcher);
    }

    @Override
    public void process(Response response, Octopus octopus) {
        File downloadDir = this.resolveSaveDir(response);
        String fileName = this.resolveSaveName(response);
        try {
            FileUtil.writeBytes(response.getBody(), new File(downloadDir, fileName));
        } catch (Exception e) {
            throw new ProcessException(
                    String.format("Can not save file [%s]", FileUtil.file(downloadDir, fileName)), e);
        }
    }

    /**
     * 获取文件保存目录
     *
     * @param response 下载响应
     * @return 保存目录
     */
    protected abstract File resolveSaveDir(Response response);

    /**
     * 获取文件保存名称
     *
     * @param response 下载响应
     * @return 文件名称
     */
    protected String resolveSaveName(Response response) {
        String fileName = getFileNameFromDisposition(response.getHeaders());
        if (StrUtil.isBlank(fileName)) {
            fileName = FileUtil.getName(response.getRequest().getUrl());
        }
        File file = new File(this.resolveSaveDir(response), fileName);
        String path = file.getPath();
        if (path.length() > MAX_FILE_PATH_LENGTH) {
            fileName =
                    UUID.fastUUID().toString(true) + "." + FileUtil.getSuffix(response.getRequest().getUrl());
        }
        return fileName;
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
