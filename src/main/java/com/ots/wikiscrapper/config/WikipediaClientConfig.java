package com.ots.wikiscrapper.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/** Configures English and Polish {@link RestClient} beans for Wikipedia REST API calls. */
@Configuration
@EnableConfigurationProperties(WikipediaProperties.class)
public class WikipediaClientConfig {

    public static final String EN_CLIENT = "wikipedia-en";
    public static final String PL_CLIENT = "wikipedia-pl";

    @Bean(EN_CLIENT)
    RestClient wikipediaEnRestClient(RestClient.Builder builder, WikipediaProperties properties) {
        return buildClient(builder, properties.enBaseUrl());
    }

    @Bean(PL_CLIENT)
    RestClient wikipediaPlRestClient(RestClient.Builder builder, WikipediaProperties properties) {
        return buildClient(builder, properties.plBaseUrl());
    }

    private static RestClient buildClient(RestClient.Builder builder, String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, "WikiScrapper/1.0 (recruitment prototype; contact: local)")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
