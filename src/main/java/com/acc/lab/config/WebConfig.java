package com.acc.lab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射根目录下的静态文件
        String projectRoot = Paths.get("").toAbsolutePath().toString();
        
        // 映射 HTML 文件
        registry.addResourceHandler("/**")
                .addResourceLocations(
                    "classpath:/static/",
                    "file:" + projectRoot + "/",
                    "file:" + projectRoot + "/photos/"
                );
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 默认首页
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}

