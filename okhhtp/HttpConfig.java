/**
 * Copyright (C): 恒大集团版权所有 Evergrande Group
 * FileName: httpConfig
 * Author:   liangyijie
 * Date:     2018-05-14 19:25
 * Description: HttpClient配置类
 */
package com.evergrande.smc.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HttpClient配置类
 *
 * @author liangyijie
 * @since 1.0.0
 *
 * httpConfig:
 *   maxTotal: 100 #最大连接数
 *   defaultMaxPerRoute: 20 #并发数
 *   connectTimeout: 30000 #创建连接的最长时间
 *   connectionRequestTimeout: 5000 #从连接池中获取到连接的最长时间
 *   socketTimeout: 30000 #数据传输的最长时间
 *   staleConnectionCheckEnabled: true #提交请求前测试连接是否可用
 *
 */

@Data
@Component
@ConfigurationProperties(prefix = "httpConfig")
public class HttpConfig {
    private Integer maxTotal;
    private Integer defaultMaxPerRoute;
    private Integer connectTimeout;
    private Integer connectionRequestTimeout;
    private Integer socketTimeout;
    private Boolean staleConnectionCheckEnabled;
    private String webserviceMyUrl;
    private String webserviceMyNamespace;
}
