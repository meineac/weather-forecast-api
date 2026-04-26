package com.forecast.client;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenWeatherClient implements WeatherDataClient {

    @Qualifier("openWeatherRestClient")
    private final RestClient restClient;

    @Override
    public BigDecimal getCurrentTemperature(BigDecimal lat, BigDecimal lon) {
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/weather")
                        .queryParam("appid", "{apiKey}")
                        .queryParam("lat", lat.toString())
                        .queryParam("lon", lon.toString())
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("openweather returned bad status: " + response.getStatusCode());
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());
            JsonNode mainNode = rootNode.get("main");

            if (mainNode == null || !mainNode.has("temp")) {
                throw new RuntimeException("failed to decode response: missing temperature data");
            }

            return new BigDecimal(mainNode.get("temp").asText());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
