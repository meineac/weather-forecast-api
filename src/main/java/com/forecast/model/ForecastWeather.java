package com.forecast.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema
public class ForecastWeather {
    private List<DailyForecast> days;

    @Getter
    @Setter
    @AllArgsConstructor
    @Schema
    public static class DailyForecast {
        private LocalDate date;
        private BigDecimal minTemperature;
        private BigDecimal maxTemperature;
    }
}