package com.forecast.controller;

import com.forecast.model.CurrentWeather;
import com.forecast.properties.CityProperties;
import com.forecast.service.LocationResolver;
import com.forecast.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void getCurrentWeather_Success() throws Exception {
        CurrentWeather mockWeather = new CurrentWeather(new BigDecimal("22.5"));
        when(service.getCurrentWeather(new BigDecimal("53.9006"), new BigDecimal("27.5590")))
                .thenReturn(mockWeather);

        mockMvc.perform(get("/api/v1/weather")
                        .param("lat", "53.9006")
                        .param("lon", "27.5590")
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
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("invalid coordinates"));
    }

    @Test
    void getCurrentWeather_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        when(service.getCurrentWeather(any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/weather")
                        .param("lat", "53.9006")
                        .param("lon", "27.5590")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void getCurrentWeatherByCity_Success() throws Exception {
        CityProperties.Coordinate mockCoords = new CityProperties.Coordinate();
        mockCoords.setLat(new BigDecimal("53.9006"));
        mockCoords.setLon(new BigDecimal("27.5590"));

        CurrentWeather mockWeather = new CurrentWeather(new BigDecimal("15.5"));

        when(locationResolver.resolve("Minsk")).thenReturn(mockCoords);
        when(service.getCurrentWeather(new BigDecimal("53.9006"), new BigDecimal("27.5590")))
                .thenReturn(mockWeather);

        mockMvc.perform(get("/api/v1/weather")
                        .param("city", "Minsk")
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
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Unsupported city: Atlantis"));
    }
}
