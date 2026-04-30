package com.forecast.service;

import com.forecast.client.ForecastDataClient;
import com.forecast.client.WeatherDataClient;
import com.forecast.model.Coordinate;
import com.forecast.model.CurrentWeather;
import com.forecast.model.ForecastWeather;
import com.forecast.properties.CityProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    @Mock
    private WeatherDataClient currentWeatherClient;

    @Mock
    private ForecastDataClient forecastDataClient;

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

        when(registry.get(PROVIDER)).thenReturn(currentWeatherClient);
        when(currentWeatherClient.getCurrentTemperature(lat, lon)).thenReturn(expectedTemp);

        CurrentWeather result = weatherService.getCurrentWeather(lat, lon, PROVIDER);

        assertEquals(expectedTemp, result.getTemperature());
        verify(registry).get(PROVIDER);
        verify(currentWeatherClient).getCurrentTemperature(lat, lon);
    }

    @Test
    void getCurrentWeather_PropagatesClientException() {
        BigDecimal lat = new BigDecimal("53.9006");
        BigDecimal lon = new BigDecimal("27.5590");

        when(registry.get(PROVIDER)).thenReturn(currentWeatherClient);
        when(currentWeatherClient.getCurrentTemperature(lat, lon)).thenThrow(new RuntimeException("API error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                weatherService.getCurrentWeather(lat, lon, PROVIDER));

        assertEquals("API error", exception.getMessage());
        verify(registry).get(PROVIDER);
        verify(currentWeatherClient).getCurrentTemperature(lat, lon);
    }

    @Test
    void getForecastWeather_ReturnsCorrectData() {
        BigDecimal lat = new BigDecimal("53.9006");
        BigDecimal lon = new BigDecimal("27.5590");

        ForecastWeather expectedForecast = new ForecastWeather(List.of(
                new ForecastWeather.DailyForecast(
                        LocalDate.now(),
                        new BigDecimal("10.0"),
                        new BigDecimal("15.0")
                )
        ));

        when(registry.get(PROVIDER)).thenReturn(forecastDataClient);
        when((forecastDataClient).getForecast(lat, lon)).thenReturn(expectedForecast);

        ForecastWeather result = weatherService.getForecastWeather(lat, lon, PROVIDER);

        assertEquals(expectedForecast.getDays().size(), result.getDays().size());
        verify(registry).get(PROVIDER);
        verify(forecastDataClient).getForecast(lat, lon);
    }

    @Test
    void getForecastWeather_UnsupportedProvider_ThrowsException() {
        BigDecimal lat = new BigDecimal("53.9006");
        BigDecimal lon = new BigDecimal("27.5590");

        when(registry.get(PROVIDER)).thenReturn(currentWeatherClient); // client mock only implements WeatherDataClient

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                weatherService.getForecastWeather(lat, lon, PROVIDER));

        assertEquals("Provider openweather does not support forecasting", exception.getMessage());
    }

    @Test
    void getCurrentWeatherBatch_ReturnsCorrectDataList() {
        // Arrange
        BigDecimal lat1 = new BigDecimal("53.9006");
        BigDecimal lon1 = new BigDecimal("27.5590");
        BigDecimal lat2 = new BigDecimal("51.5072");
        BigDecimal lon2 = new BigDecimal("-0.1275");

        List<Coordinate> coordinates = List.of(
                new Coordinate(lat1, lon1),
                new Coordinate(lat2, lon2)
        );

        when(registry.get(PROVIDER)).thenReturn(currentWeatherClient);
        when(currentWeatherClient.getCurrentTemperature(lat1, lon1)).thenReturn(new BigDecimal("15.5"));
        when(currentWeatherClient.getCurrentTemperature(lat2, lon2)).thenReturn(new BigDecimal("10.2"));

        List<CurrentWeather> result = weatherService.getCurrentWeatherBatch(coordinates, PROVIDER);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("15.5"), result.get(0).getTemperature());
        assertEquals(new BigDecimal("10.2"), result.get(1).getTemperature());
        verify(registry).get(PROVIDER);
        verify(currentWeatherClient).getCurrentTemperature(lat1, lon1);
        verify(currentWeatherClient).getCurrentTemperature(lat2, lon2);
    }
}
