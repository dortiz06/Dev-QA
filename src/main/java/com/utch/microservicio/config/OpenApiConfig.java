package com.utch.microservicio.config;

import com.utch.microservicio.util.AppConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(AppConstants.OPENAPI_TITLE)
                        .description(AppConstants.OPENAPI_DESCRIPTION)
                        .version(AppConstants.OPENAPI_VERSION));
    }
}
