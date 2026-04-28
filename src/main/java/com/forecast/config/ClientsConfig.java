package com.forecast.config;

import java.util.List;
import java.util.Map;

import com.forecast.client.WeatherDataClient;
import com.forecast.properties.GoogleWeatherProperties;
import com.forecast.service.WeatherClientRegistry;
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

    @Bean
    RestClient googleWeatherRestClient(GoogleWeatherProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultUriVariables(Map.of("apiKey", properties.getApiKey()))
                .build();
    }

    @Bean
    WeatherClientRegistry weatherClientRegistry(List<WeatherDataClient> clients) {
        return new WeatherClientRegistry(clients);
    }
}
