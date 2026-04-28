package com.forecast.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.googleweather")
public class GoogleWeatherProperties {
    private String baseUrl;
    private String apiKey;
}