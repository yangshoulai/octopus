package com.octopus.core.configurable;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.yaml.YamlUtil;
import com.octopus.core.OctopusBuilder;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import lombok.Data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Octopus 爬虫配置
 *
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class OctopusBuilderProperties implements Validatable, Transformable<OctopusBuilder> {

    /**
     * 爬虫名称
     * <p>
     * 默认 Octopus
     */
    private String name = "Octopus";

    /**
     * 使用线程数
     * <p>
     * 默认 处理器数量 * 2
     */
    private int threads = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 是否自动关闭
     * <p>
     * 默认 true
     */
    private boolean autoStop = true;

    /**
     * 启动时是否清空请求存储器
     * <p>
     * 默认 true
     */
    private boolean clearStoreOnStartup = true;

    /**
     * 停止时是否清空请求存储器
     * <p>
     * 默认 true
     */
    private boolean clearStoreOnStop = true;

    /**
     * 当请求存储器中还有未处理完的请求时是否忽略种子
     * <p>
     * 默认 true
     */
    private boolean ignoreSeedsWhenStoreHasRequests = true;

    /**
     * 是否重试失败请求
     * <p>
     * 默认 true
     */
    private boolean replayFailedRequest = true;

    /**
     * 最大重试次数
     * <p>
     * 默认 1
     */
    private int maxReplays = 1;

    /**
     * 站点配置列表
     */
    private List<WebSiteProperties> sites = new ArrayList<>();

    /**
     * 种子配置列表
     */
    private List<RequestProperties> seeds = new ArrayList<>();

    /**
     * 下载器类型
     * <p>
     * 默认 OKHttp
     */
    private DownloaderType downloader = DownloaderType.OKHttp;

    /**
     * 全局下载配置
     * <p>
     * 默认 下载配置默认参数
     */
    private DownloadProperties globalDownloadConfig = new DownloadProperties();

    /**
     * 请求存储器配置
     * <p>
     * 默认 Memory
     */
    private StoreProperties store = new StoreProperties();

    /**
     * 处理器配置列表
     * <p>
     * 默认 空
     */
    private List<ProcessorProperties> processors = new ArrayList<>();

    public OctopusBuilderProperties() {
    }

    public OctopusBuilderProperties(String name) {
        this.name = name;
    }

    public OctopusBuilderProperties(String name, int threads) {
        this.name = name;
        this.threads = threads;
    }


    @Override
    public void validate() throws ValidateException {
        if (StrUtil.isBlank(name)) {
            throw new ValidateException("octopus name is required");
        }
        if (threads <= 0) {
            throw new ValidateException("octopus threads must be greater than 0");
        }
        if (sites != null) {
            for (WebSiteProperties site : sites) {
                site.validate();
            }
        }
        if (seeds != null) {
            for (RequestProperties seed : seeds) {
                seed.validate();
            }
        }
        if (globalDownloadConfig != null) {
            globalDownloadConfig.validate();
        }
        if (store == null) {
            throw new ValidateException("octopus store is required");
        }
        store.validate();
        if (downloader == null) {
            throw new ValidateException("octopus downloader is required");
        }
        if (processors != null) {
            for (ProcessorProperties processor : processors) {
                processor.validate();
            }
        }

    }

    public static OctopusBuilderProperties fromYaml(InputStream inputStream) {
        return YamlUtil.load(inputStream, OctopusBuilderProperties.class);
    }

    public static OctopusBuilderProperties fromYaml(String filePath) {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            return fromYaml(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OctopusBuilder transform() {
        this.validate();
        OctopusBuilder builder = new OctopusBuilder();
        builder.setName(name);
        builder.setThreads(threads);
        builder.autoStop(autoStop);
        builder.clearStoreOnStartup(clearStoreOnStartup);
        builder.clearStoreOnStop(clearStoreOnStop);
        builder.ignoreSeedsWhenStoreHasRequests(ignoreSeedsWhenStoreHasRequests);
        builder.setReplayFailedRequest(replayFailedRequest);
        builder.setMaxReplays(maxReplays);
        for (WebSiteProperties site : sites) {
            builder.addSite(site.transform());
        }
        if (seeds != null) {
            for (RequestProperties seed : seeds) {
                builder.addSeeds(seed.transform());
            }
        }
        if (downloader == DownloaderType.OKHttp) {
            builder.useOkHttpDownloader();
        } else {
            builder.useHttpClientDownloader();
        }
        if (globalDownloadConfig != null) {
            builder.setGlobalDownloadConfig(globalDownloadConfig.transform());
        }
        builder.setStore(store.transform());
        if (processors != null) {
            for (ProcessorProperties processor : processors) {
                builder.addProcessor(processor.transform());
            }
        }
        return builder;
    }
}
