package com.swyp.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Location Based Target Authentication API")
                .description("위치 기반 목표 인증 API 문서")
                .version("1.0.0"))
            .addServersItem(new Server().url("/"));
    }
} 