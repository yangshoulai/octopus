package com.octopus.core.configuration;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.yaml.YamlUtil;
import com.octopus.core.OctopusBuilder;
import com.octopus.core.exception.ValidateException;
import com.octopus.core.processor.ConfigurableProcessor;
import com.octopus.core.processor.extractor.configurable.TextProcessorProperties;
import com.octopus.core.utils.Validator;
import lombok.Data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/01/12
 */
@Data
public class OctopusBuilderProperties implements Validator {

    private String name = "Octopus";

    private int threads = Runtime.getRuntime().availableProcessors() * 2;

    private boolean autoStop = true;

    private boolean clearStoreOnStartup = true;

    private boolean clearStoreOnStop = true;

    private boolean ignoreSeedsWhenStoreHasRequests = true;

    private boolean replayFailedRequest = true;

    private int maxReplays = 1;

    private List<WebSiteProperties> sites = new ArrayList<>();

    private List<RequestProperties> seeds = new ArrayList<>();

    private DownloaderType downloader = DownloaderType.OKHttp;

    private DownloadProperties globalDownloadConfig = new DownloadProperties();

    private StoreProperties store = new StoreProperties();

    private List<TextProcessorProperties> processors = new ArrayList<>();

    public OctopusBuilderProperties() {
    }

    public OctopusBuilderProperties(String name) {
        this.name = name;
    }

    public OctopusBuilderProperties(String name, int threads) {
        this.name = name;
        this.threads = threads;
    }

    public OctopusBuilder toBuilder() throws ValidateException {
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
            builder.addSite(site.toWebSite());
        }
        if (seeds != null) {
            for (RequestProperties seed : seeds) {
                builder.addSeeds(seed.toRequest());
            }
        }
        if (downloader == DownloaderType.OKHttp) {
            builder.useOkHttpDownloader();
        } else {
            builder.useHttpClientDownloader();
        }
        if (globalDownloadConfig != null) {
            builder.setGlobalDownloadConfig(globalDownloadConfig.toDownloadConfig());
        }
        builder.setStore(store.toStore());
        if (processors != null) {
            for (TextProcessorProperties processor : processors) {
                builder.addProcessor(new ConfigurableProcessor(processor));
            }
        }
        return builder;
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
        if (downloader == null) {
            throw new ValidateException("octopus downloader is required");
        }
        if (processors != null) {
            for (TextProcessorProperties processor : processors) {
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

}
