package com.octopus.core.store;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.octopus.core.Request;
import com.octopus.core.replay.ReplayFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2023/12/27
 */
public class FileSystemStore implements Store {

    private final File all;
    private final File waiting;

    private final File failed;

    private final File completed;

    private final File executing;

    public FileSystemStore(String dir) {
        this(new File(dir));
    }

    public FileSystemStore(File dir) {
        this.all = new File(dir, "all");
        this.waiting = new File(dir, "waiting");
        this.failed = new File(dir, "failed");
        this.completed = new File(dir, "completed");
        this.executing = new File(dir, "executing");
    }

    @Override
    public Request get() {
        String name = FileUtil.listFileNames(this.waiting.getPath()).stream().sorted((a, b) -> -a.compareTo(b)).findFirst().orElse(null);
        if (StrUtil.isNotBlank(name) && FileUtil.exist(this.waiting) && FileUtil.exist(this.all)) {
            String id = name.substring(name.indexOf("_") + 1);
            File file = new File(this.all, id);
            if (file.exists()) {
                String json = FileUtil.readString(file, CharsetUtil.CHARSET_UTF_8);
                FileUtil.move(new File(this.waiting, name), new File(this.executing, id), true);
                setStatus(id, Request.State.Executing, null);
                return JSONUtil.toBean(json, Request.class);
            }
        }
        return null;
    }

    @Override
    public boolean put(Request request) {
        String name = String.format("%010d", request.getPriority()) + "_" + request.getId();
        try {
            String json = JSONUtil.toJsonStr(request);
            name = String.format("%010d", request.getPriority()) + "_" + request.getId();
            FileUtil.writeString(json, this.all + File.separator + request.getId(), CharsetUtil.CHARSET_UTF_8);
            FileUtil.touch(this.waiting, name);
            return true;
        } catch (IORuntimeException e) {
            try {
                FileUtil.del(this.all + File.separator + request.getId());
            } catch (IORuntimeException e1) {
                // ignore
            }
            try {
                FileUtil.del(this.waiting + File.separator + name);
            } catch (IORuntimeException e1) {
                // ignore
            }

        }
        return false;
    }

    @Override
    public boolean exists(Request request) {
        return FileUtil.exist(this.all) ? FileUtil.exist(new File(this.all, request.getId())) : false;
    }

    @Override
    public void clear() {
        FileUtil.del(this.waiting.getParentFile());
    }

    @Override
    public void markAsCompleted(Request request) {
        setStatus(request.getId(), Request.State.Completed, null);
        FileUtil.touch(this.completed, request.getId());
    }

    @Override
    public void markAsFailed(Request request, String error) {
        setStatus(request.getId(), Request.State.Failed, null);
        FileUtil.writeString(error, new File(this.failed, request.getId()), CharsetUtil.CHARSET_UTF_8);
    }

    @Override
    public long getTotalSize() {
        return FileUtil.exist(this.all) ? FileUtil.listFileNames(this.all.getPath()).size() : 0;
    }

    @Override
    public long getCompletedSize() {
        return FileUtil.exist(this.completed) ? FileUtil.listFileNames(this.completed.getPath()).size() : 0;
    }

    @Override
    public long getWaitingSize() {
        return FileUtil.exist(this.waiting) ? FileUtil.listFileNames(this.waiting.getPath()).size() : 0;
    }

    @Override
    public long getFailedSize() {
        return FileUtil.exist(this.failed) ? FileUtil.listFileNames(this.failed.getPath()).size() : 0;
    }

    @Override
    public List<Request> getFailed() {
        if (!FileUtil.exist(this.failed)) {
            return new ArrayList<>();
        }
        return FileUtil.listFileNames(this.failed.getPath()).stream().map(id -> {
            File file = new File(this.all, id);
            if (file.exists()) {
                String json = FileUtil.readString(file, CharsetUtil.CHARSET_UTF_8);
                String err = FileUtil.readString(new File(this.failed, id), CharsetUtil.CHARSET_UTF_8);
                Request request = JSONUtil.toBean(json, Request.class);
                request.setStatus(Request.Status.of(Request.State.Failed, err));
                return request;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        FileUtil.del(new File(this.all, id));
        FileUtil.del(new File(this.failed, id));
        FileUtil.del(new File(this.completed, id));
        FileUtil.del(new File(this.executing, id));
        for (String name : FileUtil.listFileNames(this.waiting.getPath())) {
            if (name.substring(name.indexOf("_") + 1).equals(id)) {
                FileUtil.del(new File(this.waiting, name));
                break;
            }
        }
    }

    @Override
    public int replayFailed(ReplayFilter filter) {
        if (!FileUtil.exist(this.failed)) {
            return 0;
        }
        int totalFailed = 0;
        for (String id : FileUtil.listFileNames(this.failed.getPath())) {
            Request request = JSONUtil.toBean(FileUtil.readString(new File(this.all, id), CharsetUtil.CHARSET_UTF_8), Request.class);
            if (filter.filter(request)) {
                request.setFailTimes(request.getFailTimes() + 1);
                request.setStatus(Request.Status.of(Request.State.Waiting));
                this.put(request);
                totalFailed++;
                FileUtil.del(new File(this.failed, id));
            }
        }
        return totalFailed;
    }

    private void setStatus(String id, Request.State state, String error) {
        File file = new File(this.all, id);
        if (file.exists()) {
            Request request = JSONUtil.toBean(FileUtil.readString(file, CharsetUtil.CHARSET_UTF_8), Request.class);
            request.setStatus(Request.Status.of(state, error));
            FileUtil.writeString(JSONUtil.toJsonStr(request), file, CharsetUtil.CHARSET_UTF_8);
        }
    }

}
