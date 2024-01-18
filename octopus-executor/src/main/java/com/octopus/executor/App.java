package com.octopus.executor;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.octopus.core.Octopus;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.File;
import java.util.Arrays;

@Slf4j
public class App {
    public static void main(String[] args) {
        if (args.length != 2 || StrUtil.isBlank(args[0])) {
            log.error("参数错误，请使用命令执行: ./octopus.sh site.yaml");
        }
        String[] sites = ArrayUtil.sub(args, 1, args.length);
        sites = Arrays.stream(sites).filter(StrUtil::isNotBlank).toArray(String[]::new);
        if (sites.length == 0) {
            log.error("请提供配置文件");
        }
        for (String site : sites) {
            site = site.endsWith(".yaml") ? site : site + ".yaml";
            File file = new File(site);
            if (!file.isAbsolute()) {
                file = new File(args[0] + File.separator + "sites", site);
            }
            if (!file.exists()) {
                log.error("文件[%s]不存在");
                break;
            }
            log.info("execute octopus with conf file => {}", file.getPath());
            try {
                Octopus.fromYaml(file.getPath()).start();
            } catch (Exception e) {
                log.error("执行异常", e);
            }
        }
    }
}
