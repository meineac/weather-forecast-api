package com.forecast.service;

import com.forecast.properties.CityProperties;
import com.forecast.model.Coordinate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationResolver {
    private final CityProperties cityProperties;

    public Coordinate resolve(String city) {
        Coordinate coordinate = cityProperties.getCities().get(city);
        if (coordinate == null) {
            throw new IllegalArgumentException("Unsupported city: " + city);
        }
        return coordinate;
    }
}