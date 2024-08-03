package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建 AliOssUtil 对象
 */
@Configuration
@Slf4j
public class OssConfiguration {

    @Bean   // 当项目启动就执行该方法并将其交给 spring 容器管理
    @ConditionalOnMissingBean   //
    public AliOssUtil aliossUtil(AliOssProperties aliOssProperties) {
        log.info("开始创建阿里云上传工具类对象：{}", aliOssProperties);

        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}
