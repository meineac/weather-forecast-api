package com.forecast.controller;

import com.forecast.model.Coordinate;
import com.forecast.model.CurrentWeather;
import com.forecast.model.ForecastWeather;
import com.forecast.service.LocationResolver;
import com.forecast.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(WeatherController.class)
public class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService service;

    @MockitoBean
    private LocationResolver locationResolver;

    private static final String PROVIDER = "openweather";

    @Test
    void getCurrentWeather_Success() throws Exception {
        CurrentWeather mockWeather = new CurrentWeather(new BigDecimal("22.5"));
        when(service.getCurrentWeather(
                new BigDecimal("53.9006"),
                new BigDecimal("27.5590"),
                PROVIDER))
                .thenReturn(mockWeather);

        mockMvc.perform(get("/api/v1/weather")
                        .param("lat", "53.9006")
                        .param("lon", "27.5590")
                        .param("provider", PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.temperature").value(22.5));
    }

    @Test
    void getCurrentWeather_InvalidCoordinates_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/weather")
                        .param("lat", "invalid")
                        .param("lon", "27.5590")
                        .param("provider", PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("invalid coordinates"));
    }

    @Test
    void getCurrentWeather_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        when(service.getCurrentWeather(any(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/weather")
                        .param("lat", "53.9006")
                        .param("lon", "27.5590")
                        .param("provider", PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void getCurrentWeatherByCity_Success() throws Exception {
        Coordinate mockCoords = new Coordinate(new BigDecimal("53.9006"), new BigDecimal("27.5590"));

        CurrentWeather mockWeather = new CurrentWeather(new BigDecimal("15.5"));

        when(locationResolver.resolve("Minsk")).thenReturn(mockCoords);
        when(service.getCurrentWeather(new BigDecimal("53.9006"), new BigDecimal("27.5590"), PROVIDER))
                .thenReturn(mockWeather);

        mockMvc.perform(get("/api/v1/weather")
                        .param("city", "Minsk")
                        .param("provider", PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.temperature").value(15.5));
    }

    @Test
    void getCurrentWeatherByCity_InvalidCity() throws Exception {
        when(locationResolver.resolve("Atlantis")).thenThrow(
                new IllegalArgumentException("Unsupported city: Atlantis"));

        mockMvc.perform(get("/api/v1/weather")
                        .param("city", "Atlantis")
                        .param("provider", PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Unsupported city: Atlantis"));
    }

    @Test
    void getForecastWeather_Success() throws  Exception {
        ForecastWeather mockForecast = new ForecastWeather(List.of(
                new ForecastWeather.DailyForecast(
                        LocalDate.of(2023, 4, 25),
                        new BigDecimal("10.0"),
                        new BigDecimal("15.0")
                )
        ));

        when(service.getForecastWeather(
                new BigDecimal("53.9006"),
                new BigDecimal("27.5590"),
                PROVIDER))
                .thenReturn(mockForecast);

        mockMvc.perform(get("/api/v1/forecast")
                        .param("lat", "53.9006")
                        .param("lon", "27.5590")
                        .param("provider", PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.days[0].minTemperature").value(10.0))
                .andExpect(jsonPath("$.data.days[0].maxTemperature").value(15.0));
    }

    @Test
    void getForecastWeatherByCity_Success() throws Exception {
        Coordinate mockCoords = new Coordinate(new BigDecimal("53.9006"), new BigDecimal("27.5590"));

        ForecastWeather mockForecast = new ForecastWeather(List.of());

        when(locationResolver.resolve("Minsk")).thenReturn(mockCoords);
        when(service.getForecastWeather(new BigDecimal("53.9006"), new BigDecimal("27.5590"), PROVIDER))
                .thenReturn(mockForecast);

        mockMvc.perform(get("/api/v1/forecast")
                        .param("city", "Minsk")
                        .param("provider", PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

    }

    @Test
    void getCurrentWeatherBatch_Success() throws Exception {
        CurrentWeather mockWeatherMinsk = new CurrentWeather(new BigDecimal("22.5"));
        CurrentWeather mockWeatherLondon = new CurrentWeather(new BigDecimal("15.0"));

        when(service.getCurrentWeatherBatch(anyList(), eq(PROVIDER)))
                .thenReturn(List.of(mockWeatherMinsk, mockWeatherLondon));

        mockMvc.perform(get("/api/v1/weather/batch")
                        .param("cities", "Minsk", "London")
                        .param("provider", PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))

                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))

                .andExpect(jsonPath("$.data[0].temperature").value(22.5))
                .andExpect(jsonPath("$.data[1].temperature").value(15.0));
    }
}
