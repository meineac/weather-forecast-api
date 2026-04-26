package com.forecast.controller;

import java.math.BigDecimal;

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

    @GetMapping("/weather")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Current Weather", description = "Returns current weather for given coordinates")
    public SuccessResponse<CurrentWeather> getCurrentWeather(
            @Parameter(description = "Latitude", required = true, example = "53.9006") @RequestParam BigDecimal lat,
            @Parameter(description = "Longitude", required = true, example = "27.5590") @RequestParam BigDecimal lon) {
        CurrentWeather result = service.getCurrentWeather(lat, lon);
        return new SuccessResponse<>(200, "Success", result);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public StatusResponse handleArgumantTypeException(Exception exception) {
        return new StatusResponse(400, "invalid coordinates");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public StatusResponse handleException(Exception exception) {
        return new StatusResponse(500, exception.getMessage());
    }
}
