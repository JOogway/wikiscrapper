package com.ots.wikiscrapper.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides the OpenAPI/Swagger metadata bean for the WikiScrapper REST endpoints. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI wikiScrapperOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("WikiScrapper API")
                        .version("1.0")
                        .description("English Wikipedia summaries for Polish voivodeships and world countries."));
    }
}
