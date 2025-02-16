package sweep.demo.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("JWT")) // JWT 보안 설정 추가
                .components(new Components().addSecuritySchemes("JWT", // JWT 보안 스키마 추가
                        new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP) // "SecurityScheme.Type.HTTP" 사용
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ))
                .info(new Info()
                        .title("Location Based Target Authentication API")
                        .description("위치 기반 목표 인증 서비스 API 문서")
                        .version("1.0.0"));
    }
}


