/**
 * Copyright (C): 恒大集团版权所有 Evergrande Group
 * FileName: OkHtppConfig
 * Author:   liangyijie
 * Date:     2018-01-18 17:22
 * Description: okhttp配置
 */
package com.evergrande.smc.common.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * okhttp配置
 *
 * @author liangyijie
 * @since 1.0.0
 */
@Configuration
public class OkHttpConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30,TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        return builder.build();
    }

}
