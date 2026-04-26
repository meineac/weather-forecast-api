package com.forecast.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.forecast.client.WeatherDataClient;
import com.forecast.model.CurrentWeather;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final WeatherDataClient client;

    public CurrentWeather getCurrentWeather(BigDecimal lat, BigDecimal lon) {
        BigDecimal temperature = client.getCurrentTemperature(lat, lon);
        return new CurrentWeather(temperature);
    }
}
