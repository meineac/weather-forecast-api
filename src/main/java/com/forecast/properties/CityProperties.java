package com.forecast.properties;

import com.forecast.model.Coordinate;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Component
@ConfigurationProperties(prefix = "app")
public class CityProperties {
    private final Map<String, Coordinate> cities = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);


}
