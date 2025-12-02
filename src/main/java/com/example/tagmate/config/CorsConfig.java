package com.example.tagmate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 * @author 86150
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 是否发送Cookie
                .allowCredentials(true)
                // 放行哪些原始域
                .allowedOriginPatterns("*")
                // 放行哪些请求方式
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 放行哪些原始请求头部信息
                .allowedHeaders("*")
                // 暴露哪些头部信息
                .exposedHeaders("*")
                // 预检请求的有效期，单位为秒。
                .maxAge(3600);
    }
}