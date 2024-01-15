package com.octopus.sample.gitee;

import com.octopus.core.configurable.OctopusBuilderProperties;
import com.octopus.core.exception.ValidateException;

import java.io.IOException;

/**
 * 获取Gitee所有推荐项目 https://gitee.com/explore/all
 *
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class GiteeProject2 {


    public static void main(String[] args) throws IOException, ValidateException {
        OctopusBuilderProperties builder = OctopusBuilderProperties.fromYaml(GiteeProject2.class.getResourceAsStream("/gitee/octopus.yaml"));
        builder.transform().build().start();
    }
}
