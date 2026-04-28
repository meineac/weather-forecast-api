package com.forecast.service;

import com.forecast.client.WeatherDataClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WeatherClientRegistry {
    private final Map<String, WeatherDataClient> clients;

    public WeatherClientRegistry(List<WeatherDataClient> clients) {
        this.clients = clients.stream()
                .collect(Collectors.toMap(
                        WeatherDataClient::getProviderName,
                        Function.identity(),
                        (a, _) -> {
                            throw new IllegalArgumentException("Duplicate provider name: " + a.getProviderName());
                        }
                ));
    }

    public WeatherDataClient get(String provider) {
        if (provider == null || !clients.containsKey(provider)) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }
        return clients.get(provider);
    }
}
