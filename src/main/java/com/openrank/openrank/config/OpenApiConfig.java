package com.openrank.openrank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("在此输入 Bearer Token，示例：Bearer xxxxxx 或直接 xxxxxx");
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth", bearer))
                .info(new Info()
                        .title("OpenRank API")
                        .description("OpenRank 接口文档")
                        .version("v1.0.0"));
    }
}
