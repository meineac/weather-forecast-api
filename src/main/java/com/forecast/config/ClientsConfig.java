package com.forecast.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.forecast.properties.OpenWeatherProperties;

@Configuration
public class ClientsConfig {

    @Bean
    RestClient openWeatherRestClient(OpenWeatherProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultUriVariables(Map.of("apiKey", properties.getApiKey()))
                .build();
    }
}
