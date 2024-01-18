package com.octopus.core.properties;

import cn.hutool.setting.yaml.YamlUtil;
import com.octopus.core.OctopusBuilder;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.utils.Transformable;
import com.octopus.core.utils.Validatable;
import com.octopus.core.utils.Validator;
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
     * 最大爬取深度 -1 无限制
     */
    private int maxDepth = -1;

    /**
     * 站点配置列表
     */
    private List<WebSiteProperties> sites = new ArrayList<>();

    /**
     * 种子配置列表
     */
    private List<RequestProperties> seeds = new ArrayList<>();

    /**
     * 下载器配置
     * <p>
     * 默认 OKHttp
     */
    private DownloaderProperties downloader = new DownloaderProperties();

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
    public void validate() throws ValidateException {
        Validator.notBlank(name, "octopus name is required");
        Validator.gt(threads, 0, "octopus threads must be greater than 0");
        Validator.validateWhenNotNull(sites);
        Validator.validateWhenNotNull(seeds);
        Validator.notEmpty(store, "octopus store is required");
        Validator.validateWhenNotNull(store);
        Validator.notEmpty(downloader, "octopus downloader is required");
        Validator.validateWhenNotNull(processors);
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
        builder.setMaxDepth(maxDepth);
        for (WebSiteProperties site : sites) {
            builder.addSite(site.transform());
        }
        if (seeds != null) {
            for (RequestProperties seed : seeds) {
                builder.addSeeds(seed.transform());
            }
        }
        if (downloader.getType() == DownloaderType.OkHttp) {
            builder.useOkHttpDownloader();
        } else {
            builder.useHttpClientDownloader();
        }
        builder.setGlobalDownloadConfig(downloader.transform());
        builder.setStore(store.transform());
        if (processors != null) {
            for (ProcessorProperties processor : processors) {
                builder.addProcessor(processor.transform());
            }
        }
        return builder;
    }
}
