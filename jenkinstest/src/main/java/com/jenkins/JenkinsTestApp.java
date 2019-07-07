package com.jenkins;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author mengximao
 * @since 1.0.0
 */
@SpringBootApplication
public class JenkinsTestApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(JenkinsTestApp.class,args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {

        return builder.sources(JenkinsTestApp.class);
    }
}
