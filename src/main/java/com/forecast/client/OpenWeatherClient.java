package com.forecast.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.forecast.model.ForecastWeather;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenWeatherClient implements WeatherDataClient, ForecastDataClient {

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
                .onStatus(HttpStatusCode::isError, (_, resp) -> {
                    throw new RuntimeException("openweather returned bad status: " + resp.getStatusCode());
                })
                .toEntity(String.class);

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

    @Override
    public String getProviderName() {
        return "openweather";
    }

    @Override
    public ForecastWeather getForecast(BigDecimal lat, BigDecimal lon) {
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/forecast")
                        .queryParam("appid", "{apiKey}")
                        .queryParam("lat", lat.toString())
                        .queryParam("lon", lon.toString())
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .toEntity(String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());
            JsonNode listNode = rootNode.get("list");

            if (listNode == null || !listNode.isArray() || listNode.isEmpty()) {
                throw new RuntimeException("failed to decode response: missing forecast list");
            }

            Map<LocalDate, List<JsonNode>> groupedByDay = new LinkedHashMap<>();
            for (var node : listNode) {
                long timestamp = node.get("dt").asLong();
                LocalDate date = LocalDate.ofEpochDay(timestamp / 86400L);

                groupedByDay.computeIfAbsent(date, _ -> new java.util.ArrayList<>()).add(node);
            }

            List<ForecastWeather.DailyForecast> days = new ArrayList<>();

            for (var entry : groupedByDay.entrySet()) {
                BigDecimal dayMin = null;
                BigDecimal dayMax = null;

                for (var node : entry.getValue()) {
                    JsonNode mainNode = node.get("main");
                    BigDecimal min = new BigDecimal(mainNode.get("temp_min").asText());
                    BigDecimal max = new BigDecimal(mainNode.get("temp_max").asText());

                    if (dayMin == null || min.compareTo(dayMin) < 0) dayMin = min;
                    if (dayMax == null || max.compareTo(dayMax) > 0) dayMax = max;
                }

                days.add(new ForecastWeather.DailyForecast(entry.getKey(), dayMin, dayMax));
            }

            return new ForecastWeather(days);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
