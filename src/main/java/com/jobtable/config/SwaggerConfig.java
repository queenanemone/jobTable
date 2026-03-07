package com.jobtable.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("학급 경제 교육 플랫폼 API")
                        .description("직업 시스템 기반 학급 경제 교육 플랫폼 REST API")
                        .version("v1.0.0"));
    }
}
