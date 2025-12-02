package com.example.tagmate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * Knife4j配置类
 *
 * @author TagMate_backed
 * @since 2024-01-01
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("用户中心管理系统 API")
                        .description("用户中心管理系统的RESTful API文档，提供用户管理、认证等功能")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发者")
                                .email("developer@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目Wiki文档")
                        .url("https://example.com/wiki"))
                .components(new Components()
                        .addSecuritySchemes("session-auth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Session认证：请先调用登录接口获取Session，然后再调用需要认证的接口")))
                .servers(new ArrayList<Server>() {{
                    add(new Server()
                            .url("http://localhost:8081")
                            .description("本地开发环境"));
                    add(new Server()
                            .url("https://api.example.com")
                            .description("生产环境"));
                }});
    }
}