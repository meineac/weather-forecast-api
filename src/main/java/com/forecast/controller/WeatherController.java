package com.forecast.controller;

import java.math.BigDecimal;
import java.util.List;

import com.forecast.model.Coordinate;
import com.forecast.model.ForecastWeather;
import com.forecast.service.LocationResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.forecast.service.WeatherService;
import com.forecast.dto.StatusResponse;
import com.forecast.dto.SuccessResponse;
import com.forecast.model.CurrentWeather;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "weather", description = "Weather API")
public class WeatherController {
    private final WeatherService service;
    private final LocationResolver locationResolver;

    @GetMapping(value = "/weather")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Weather", description = "Get weather by city name OR lat/lon coordinates")
    public SuccessResponse<CurrentWeather> getCurrentWeather(
            @Parameter(description = "Latitude", example = "53.9006")
            @RequestParam(required = false) BigDecimal lat,

            @Parameter(description = "Longitude", example = "27.5590")
            @RequestParam(required = false) BigDecimal lon,

            @Parameter(description = "City name", example = "Minsk")
            @RequestParam(required = false) String city,

            @Parameter(description = "Provider name", required = true, example = "openweather")
            @RequestParam String provider) {

        if (lat != null && lon != null) {
            return new SuccessResponse<>(200, "Success", service.getCurrentWeather(lat, lon, provider));
        } else if (city != null) {
            var coords = locationResolver.resolve(city);
            return new SuccessResponse<>(
                    200,
                    "Success",
                    service.getCurrentWeather(coords.getLat(), coords.getLon(), provider)
            );
        } else {
            throw new IllegalArgumentException("Provide either 'city' or both 'lat' and 'lon'");
        }
    }

    @GetMapping("/forecast")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Forecast", description = "Get weather forecast by city name OR lat/lon coordinates")
    public SuccessResponse<ForecastWeather> getForecastWeather(
            @Parameter(description = "Latitude", example = "53.9006")
            @RequestParam(required = false) BigDecimal lat,

            @Parameter(description = "Longitude", example = "27.5590")
            @RequestParam(required = false) BigDecimal lon,

            @Parameter(description = "City name", example = "Minsk")
            @RequestParam(required = false) String city,

            @Parameter(description = "Provider name", required = true, example = "openweather")
            @RequestParam String provider) {
        if (lat != null && lon != null) {
            return new SuccessResponse<>(
                    200,
                    "Success",
                    service.getForecastWeather(lat, lon, provider)
            );
        } else if (city != null) {
            var coords = locationResolver.resolve(city);
            return new SuccessResponse<>(
                    200,
                    "Success",
                    service.getForecastWeather(coords.getLat(), coords.getLon(), provider)
            );
        } else {
            throw new IllegalArgumentException("Provide either 'city' or both 'lat' and 'lon'");
        }
    }

    @GetMapping("/weather/batch")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Current Weather Batch", description = "Get current weather for a list of locations")
    public SuccessResponse<List<CurrentWeather>> getCurrentWeatherBatch(
            @Parameter(description = "List of city names", required = true, example = "Minsk,London")
            @RequestParam List<String> cities,

            @Parameter(description = "Provider name", required = true, example = "openweather")
            @RequestParam String provider) {
        if (cities == null || cities.isEmpty()) {
            throw new IllegalArgumentException("Provide a list of cities");
        }

        List<Coordinate> coordinates = cities.stream()
                .map(locationResolver::resolve)
                .toList();

        List<CurrentWeather> results = service.getCurrentWeatherBatch(coordinates, provider);

        return new SuccessResponse<>(200, "Success", results);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public StatusResponse handleArgumentTypeException() {
        return new StatusResponse(400, "invalid coordinates");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public StatusResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        return new StatusResponse(400, exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public StatusResponse handleException(Exception exception) {
        return new StatusResponse(500, exception.getMessage());
    }
}
