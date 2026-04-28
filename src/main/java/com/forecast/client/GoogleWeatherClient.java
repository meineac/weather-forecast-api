package com.forecast.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class GoogleWeatherClient implements WeatherDataClient {
    @Qualifier("googleWeatherRestClient")
    private final RestClient restClient;

    @Override
    public BigDecimal getCurrentTemperature(BigDecimal lat, BigDecimal lon) {
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/currentConditions:lookup")
                        .queryParam("key", "{apiKey}")
                        .queryParam("location.latitude", lat.toString())
                        .queryParam("location.longitude", lon.toString())
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (_, resp) -> {
                    throw new RuntimeException("googleweather returned bad status: " + resp.getStatusCode());
                })
                .toEntity(String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());
            JsonNode temperatureNode = rootNode.get("temperature");

            if (temperatureNode == null || !temperatureNode.has("degrees")) {
                throw new RuntimeException("failed to decode response: missing temperature data");
            }

            return new BigDecimal(temperatureNode.get("degrees").asText());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProviderName() {
        return "googleweather";
    }
}
