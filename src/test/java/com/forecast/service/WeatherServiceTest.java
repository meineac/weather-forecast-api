package com.forecast.service;

import com.forecast.client.WeatherDataClient;
import com.forecast.model.CurrentWeather;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    @Mock
    private WeatherDataClient client;

    @Mock
    private WeatherClientRegistry registry;

    @InjectMocks
    private WeatherService weatherService;

    private static final String PROVIDER = "openweather";

    @Test
    void getCurrentWeather_ReturnsCorrectData() {
        BigDecimal lat = new BigDecimal("53.9006");
        BigDecimal lon = new BigDecimal("27.5590");
        BigDecimal expectedTemp = new BigDecimal("15.5");

        when(registry.get(PROVIDER)).thenReturn(client);
        when(client.getCurrentTemperature(lat, lon)).thenReturn(expectedTemp);

        CurrentWeather result = weatherService.getCurrentWeather(lat, lon, PROVIDER);

        assertEquals(expectedTemp, result.getTemperature());
        verify(registry).get(PROVIDER);
        verify(client).getCurrentTemperature(lat, lon);
    }

    @Test
    void getCurrentWeather_PropagatesClientException() {
        BigDecimal lat = new BigDecimal("53.9006");
        BigDecimal lon = new BigDecimal("27.5590");

        when(registry.get(PROVIDER)).thenReturn(client);
        when(client.getCurrentTemperature(lat, lon)).thenThrow(new RuntimeException("API error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                weatherService.getCurrentWeather(lat, lon, PROVIDER));

        assertEquals("API error", exception.getMessage());
        verify(registry).get(PROVIDER);
        verify(client).getCurrentTemperature(lat, lon);
    }
}
