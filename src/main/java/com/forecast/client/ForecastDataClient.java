package com.forecast.client;

import com.forecast.model.ForecastWeather;

import java.math.BigDecimal;

public interface ForecastDataClient {
    ForecastWeather getForecast(BigDecimal lat, BigDecimal lon);
}
