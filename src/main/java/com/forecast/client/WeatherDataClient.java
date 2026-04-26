package com.forecast.client;

import java.math.BigDecimal;

public interface WeatherDataClient {
    BigDecimal getCurrentTemperature(BigDecimal lat, BigDecimal lon);
}
