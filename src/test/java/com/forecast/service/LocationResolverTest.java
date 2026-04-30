package com.forecast.service;

import com.forecast.properties.CityProperties;
import com.forecast.model.Coordinate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocationResolverTest {

    private static LocationResolver resolver;

    @BeforeAll
    static void setUp() {
        CityProperties properties = new CityProperties();
        Coordinate minskCoords = new Coordinate();
        minskCoords.setLat(new BigDecimal("53.9006"));
        minskCoords.setLon(new BigDecimal("27.5590"));
        properties.getCities().put("Minsk", minskCoords);
        resolver = new LocationResolver(properties);
    }

    @Test
    void resolve_ValidCity() {
        Coordinate result = resolver.resolve("Minsk");

        assertEquals(new BigDecimal("53.9006"), result.getLat());
        assertEquals(new BigDecimal("27.5590"), result.getLon());
    }

    @Test
    void resolve_InvalidCity() {

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("UnknownCity"));
    }

    @Test
    void resolve_CaseInsensitiveCity() {
        Coordinate result = resolver.resolve("mInSk");
        assertEquals(new BigDecimal("53.9006"), result.getLat());
        assertEquals(new BigDecimal("27.5590"), result.getLon());
    }
}