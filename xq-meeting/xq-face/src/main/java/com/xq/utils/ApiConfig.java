package com.xq.utils;

import com.baidu.aip.face.AipFace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 百度sdk配置
 * @Author: ysf
 * @CreateTime: 2025-03-20  16:59
 * @Version: 1.0
 */
@Configuration
public class ApiConfig {
    @Value("${baidu.face.appId}")
    private String appId;

    @Value("${baidu.face.apiKey}")
    private String apiKey;

    @Value("${baidu.face.secretKey}")
    private String secretKey;

    @Bean
    public AipFace aipFace() {
        return new AipFace(appId, apiKey, secretKey);
    }
}
